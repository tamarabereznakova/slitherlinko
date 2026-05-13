(function () {
    'use strict';

    //STAV
    const announcedMessages = new Set();
    const announcedEvents = new Set();
    let currentUser = null;

    //TOAST KONFIG
    const MAX_VISIBLE = 3;
    const TOAST_DURATION = 5000;
    const FADE_DURATION = 600;
    const queue = [];
    const activeToasts = [];   //fifo, 0 najstarsi, last najnovsi

    //  BROWSER NOTIFY 
    function requestPermission() {
        if (!('Notification' in window)) return;
        if (Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }

    function maybeBrowserNotify(item) {
        if (!('Notification' in window)) return;
        if (Notification.permission !== 'granted') return;
        if (!document.hidden) return;
        try {
            const n = new Notification(item.title, {
                body: item.body,
                icon: '/images/snake.png',
                silent: false
            });
            if (item.onClick) {
                n.onclick = function () {
                    window.focus();
                    item.onClick();
                    n.close();
                };
            }
            setTimeout(() => n.close(), 5000);
        } catch (e) { /* ignore */
        }
    }

    function relayoutToasts() {
        let bottom = 20;
        for (let i = activeToasts.length - 1; i >= 0; i--) {
            const t = activeToasts[i];
            if (!t._removed) {
                t.style.bottom = bottom + 'px';
                bottom += t.offsetHeight + 10;
            }
        }
    }

    function removeToast(toast) {
        if (toast._removed) return;
        toast._removed = true;
        toast.style.transition = 'opacity ' + FADE_DURATION + 'ms ease, transform ' + FADE_DURATION + 'ms ease';
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(-30%)';
        setTimeout(() => {
            const idx = activeToasts.indexOf(toast);
            if (idx >= 0) activeToasts.splice(idx, 1);
            if (toast.parentNode) toast.parentNode.removeChild(toast);
            relayoutToasts();
            processQueue();
        }, FADE_DURATION);
    }

    function processQueue() {
        while (activeToasts.length < MAX_VISIBLE && queue.length > 0) {
            const item = queue.shift();
            renderToast(item);
        }
    }

    function enqueue(item) {
        queue.push(item);
        processQueue();
    }

    function renderToast(item) {
        const c = item.color || '#00f3ff';
        const toast = document.createElement('div');
        toast.style.cssText = [
            'position:fixed',
            'left:20px',
            'bottom:-200px',
            'background:#0f0f1a',
            'border:2px solid ' + c,
            'box-shadow:0 0 16px ' + c + '66',
            'border-radius:6px',
            'padding:12px 16px',
            'min-width:240px',
            'max-width:340px',
            'z-index:9999',
            'cursor:pointer',
            'font-family:"Rajdhani",sans-serif',
            'opacity:0',
            'transform:translateX(-30%)',
            'transition:opacity 0.4s ease, transform 0.4s ease, bottom 0.3s ease'
        ].join(';');

        const t = document.createElement('div');
        t.style.cssText = 'font-family:"Orbitron",monospace;font-size:0.6rem;color:' + c + ';margin-bottom:4px;letter-spacing:1px;';
        t.textContent = item.title;
        toast.appendChild(t);

        const b = document.createElement('div');
        b.style.cssText = 'font-size:0.8rem;color:#ffffff;line-height:1.3;';
        b.textContent = item.body;
        toast.appendChild(b);

        const close = document.createElement('div');
        close.style.cssText = 'position:absolute;top:6px;right:8px;cursor:pointer;color:#ff00aa;font-size:0.8rem;line-height:1;';
        close.textContent = '✕';
        close.onclick = function (e) {
            e.stopPropagation();
            removeToast(toast);
        };
        toast.appendChild(close);

        if (item.onClick) {
            toast.onclick = function () {
                item.onClick();
                removeToast(toast);
            };
        }

        document.body.appendChild(toast);
        activeToasts.push(toast);

        // Anim act
        requestAnimationFrame(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateX(0)';
            relayoutToasts();
        });

        setTimeout(() => removeToast(toast), TOAST_DURATION);
        maybeBrowserNotify(item);
    }

    function buildMailboxToast(m) {
        let title = '📡 NEW SPECTATE LINK';
        let color = '#00f3ff';
        if (m.inviteType === 'LOBBY_RACE') {
            title = '⚔ NEW RACE INVITE';
            color = '#ff00aa';
        } else if (m.inviteType === 'LOBBY_COOP') {
            title = 'NEW CO-OP INVITE';
            color = '#00f3ff';
        }
        return {
            title: title,
            body: 'From: ' + m.from,
            color: color,
            onClick: function () {
                window.location.href = '/slitherlink/inbox';
            }
        };
    }

    function buildEventToast(ev) {
        if (ev.type === 'FOLLOW') {
            return {
                title: 'NEW FOLLOWER',
                body: ev.actor + ' just followed you!',
                color: '#ffee00',
                onClick: function () {
                    window.location.href = '/slitherlink/profile/' + ev.actor;
                }
            };
        } else if (ev.type === 'UNFOLLOW') {
            return {
                title: 'LOST A FOLLOWER',
                body: ev.actor + ' unfollowed you.',
                color: '#ff00aa',
                onClick: function () {
                    window.location.href = '/slitherlink/profile/' + ev.actor;
                }
            };
        } else if (ev.type === 'COMMENT') {
            const preview = ev.payload && ev.payload.length > 60
                ? ev.payload.substring(0, 60) + '...'
                : ev.payload;
            return {
                title: 'NEW WALL POST',
                body: ev.actor + ' wrote: "' + (preview || '') + '"',
                color: '#ff00aa',
                onClick: function () {
                    window.location.href = '/slitherlink/profile/' + currentUser;
                }
            };
        } else if (ev.type === 'ACHIEVEMENT') {
            const map = {
                first_loop: ['🌱', 'First Loop'],
                on_fire: ['🔥', 'On Fire'],
                veteran: ['💎', 'Veteran'],
                master: ['🏆', 'Master'],
                speed_demon: ['⚡', 'Speed Demon'],
                marathon: ['🐌', 'Marathon'],
                tutorial: ['🎓', 'Tutorial Complete'],
                brainiac: ['🧠', 'Brainiac'],
                god: ['🌌', 'God'],
                team_player: ['👥', 'Team Player'],
                coop_champion: ['🤝', 'Co-op Champion'],
                first_blood: ['🩸', 'First Blood'],
                race_master: ['👑', 'Race Master'],
                social_butterfly: ['🌟', 'Social Butterfly'],
                popular: ['💜', 'Popular'],
                wall_writer: ['📝', 'Wall Writer']
            };
            const info = map[ev.payload] || ['🏅', ev.payload];
            return {
                title: 'ACHIEVEMENT UNLOCKED',
                body: info[0] + ' ' + info[1],
                color: '#ffee00',
                onClick: function () {
                    window.location.href = '/slitherlink/profile/' + currentUser;
                }
            };
        }
        return null;
    }

    //POLLING
    async function poll() {
        try {
            const res = await fetch('/api/notifications', {cache: 'no-store'});
            const data = await res.json();
            if (!data.loggedIn) return;

            // Unread
            const badge = document.getElementById('unread-badge');
            if (badge) {
                if (data.unread > 0) {
                    badge.textContent = data.unread;
                    badge.style.display = 'flex';
                } else {
                    badge.style.display = 'none';
                }
            }

            const unreadList = data.unreadAll || (data.latestUnread ? [data.latestUnread] : []);

            const sortedUnread = unreadList.slice().reverse();
            for (const m of sortedUnread) {
                if (!announcedMessages.has(m.ident)) {
                    announcedMessages.add(m.ident);
                    enqueue(buildMailboxToast(m));
                }
            }

            //eventy na PROFiLE
            if (data.events && data.events.length > 0) {
                const sorted = data.events.slice().reverse();
                for (const ev of sorted) {
                    if (!announcedEvents.has(ev.ident)) {
                        announcedEvents.add(ev.ident);
                        const item = buildEventToast(ev);
                        if (item) enqueue(item);
                    }
                }
            }
        } catch (e) {
            console.error('poll error', e);
        }
    }

    // ONLINE
    async function updateOnlineDots() {
        const dots = document.querySelectorAll('[data-online-user]');
        const checked = new Set();
        for (const dot of dots) {
            const user = dot.getAttribute('data-online-user');
            if (!user || checked.has(user)) continue;
            checked.add(user);
            try {
                const res = await fetch('/api/online?user=' + encodeURIComponent(user),
                    {cache: 'no-store'});
                const data = await res.json();
                const online = data.online === true;
                document.querySelectorAll('[data-online-user="' + CSS.escape(user) + '"]').forEach(d => {
                    d.style.background = online ? '#00ff88' : '#555';
                    d.style.boxShadow = online ? '0 0 6px #00ff88' : 'none';
                    d.title = online ? user + ' is online' : user + ' is offline';
                });
            } catch (e) {
            }
        }
    }

    function detectCurrentUser() {
        const el = document.querySelector('.player-name span');
        if (el) currentUser = el.textContent.trim();
    }

    function init() {
        requestPermission();
        detectCurrentUser();
        poll();
        updateOnlineDots();
        setTimeout(poll, 2000);
        setInterval(poll, 10000);
        setInterval(updateOnlineDots, 5000);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();