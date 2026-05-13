(function () {
    const ADS = [
        {
            img:      "/images/ad1.png",
            headline: "PLAY\nSLITHERLINK",
            sub:      "The loop MUST close.\nCan YOU do it???"
        },
        {
            img:      "/images/ad2.png",
            headline: "TOP 10\nAWAITS YOU",
            sub:      "Your name on the leaderboard.\nGlory.\nBecome a GOD"
        },
        {
            img:      "/images/ad6.png",
            headline: "10x10 DARE YOU?",
            sub:      "The hardest loop\nyou'll ever close.",
        },
        {
            img:      "/images/ad4.png",
            headline: "ARE YOU\nSMART\nENOUGH?",
            sub:      "Slitherlink doesn't forgive mistakes."
        },
        {
            img:      "/images/ad5.png",
            headline: "JOIN THE\nSNAKE CULT",
        },
        {
            img:      "/images/ad3.png",
            headline: "MORE than just a game.\nNO MERCY.",
            sub:      "play with your friends\nWho wins ???"
        },
        {
            img:      "/images/ad7.png",
            headline: "TRY OUR CUTEST GAME EVER",
            sub:      "roll your cube\n WIN ALL LEVELS !"
        },
    ];

    const isNeon = () => (localStorage.getItem('theme') || 'neon') !== 'pastel';
    const THEMES = {
        neon: {
            colBg:      '#0a0a0f',
            colBorder:  '#00f3ff',
            bg:         '#0f0f1a',
            border:     '#00f3ff',
            borderGlow: 'rgba(0,243,255,0.5)',
            headline:   '#00f3ff',
            sub:        '#a0a0cc',
            pixel:      '#ff00aa',
            scanline:   'rgba(0,243,255,0.03)',
            label:      'AD BLOCK',
            labelColor: '#ff00aa',
            font:       "'Orbitron', monospace",
            subFont:    "'Rajdhani', sans-serif",
            divider:    'rgba(0,243,255,0.15)',
        },
        pastel: {
            colBg:      '#f5eeec',
            colBorder:  '#e8998a',
            bg:         '#faf5f4',
            border:     '#b85c6a',
            borderGlow: 'rgba(184,92,106,0.25)',
            headline:   '#b85c6a',
            sub:        '#7a5050',
            pixel:      '#e8998a',
            scanline:   'rgba(232,153,138,0.04)',
            label:      'reklama',
            labelColor: '#e8998a',
            font:       "'DM Serif Display', serif",
            subFont:    "'Quicksand', sans-serif",
            divider:    'rgba(184,92,106,0.12)',
        }
    };

    /*INIT*/
    const COL_WIDTH    = 240;
    const SCROLL_SPEED = 0.5;
    let colEl   = null;
    let trackEl = null;
    let scrollY = 0;
    let rafId   = null;
    let trackH  = 0;

    function buildCard(ad, t) {
        const card = document.createElement('div');
        card.style.cssText = [
            'width:' + (COL_WIDTH - 20) + 'px',
            'margin:0 auto 18px auto',
            'background:' + t.bg,
            'border:2px solid ' + t.border,
            'box-shadow:0 0 0 2px ' + t.bg + ',0 0 0 4px ' + t.border + ',0 0 14px ' + t.borderGlow,
            'position:relative',
            'overflow:hidden',
        ].join(';');

        const scan = document.createElement('div');
        scan.style.cssText = [
            'position:absolute', 'inset:0', 'pointer-events:none', 'z-index:2',
            'background:repeating-linear-gradient(to bottom,' +
            t.scanline + ' 0px,' + t.scanline + ' 1px,transparent 1px,transparent 3px)',
        ].join(';');
        card.appendChild(scan);

        const body = document.createElement('div');
        body.style.cssText = 'padding:10px 8px 10px;text-align:center;position:relative;z-index:3;';

        if (ad.img) {
            const img = document.createElement('img');
            img.src = ad.img;
            img.style.cssText = 'width:100%;image-rendering:pixelated;display:block;margin-bottom:6px;';
            body.appendChild(img);
        }

        const h = document.createElement('div');
        h.style.cssText = [
            'font-family:' + t.font,
            'font-size:0.58rem',
            'color:' + t.headline,
            'line-height:1.4',
            'margin-bottom:5px',
            'white-space:pre-line',
            'letter-spacing:1px',
        ].join(';');
        h.textContent = ad.headline;
        body.appendChild(h);

        const s = document.createElement('div');
        s.style.cssText = [
            'font-family:' + t.subFont,
            'font-size:0.52rem',
            'color:' + t.sub,
            'opacity:0.85',
            'line-height:1.4',
            'white-space:pre-line',
        ].join(';');
        s.textContent = ad.sub;
        body.appendChild(s);

        card.appendChild(body);

        [
            ['top:4px;left:4px',     'border-top',    'border-left'],
            ['top:4px;right:4px',    'border-top',    'border-right'],
            ['bottom:4px;left:4px',  'border-bottom', 'border-left'],
            ['bottom:4px;right:4px', 'border-bottom', 'border-right'],
        ]
            .forEach(function(c) {
            const dot = document.createElement('div');
            dot.style.cssText = 'position:absolute;' + c[0] + ';width:5px;height:5px;' +
                c[1] + ':2px solid ' + t.pixel + ';' +
                c[2] + ':2px solid ' + t.pixel + ';z-index:4;';
            card.appendChild(dot);
        });

        return card;
    }

    function buildColumn() {
        const t = isNeon() ? THEMES.neon : THEMES.pastel;

        const col = document.createElement('div');
        col.id = 'slink-ad-col';
        col.style.cssText = [
            'position:fixed',
            'top:0',
            'right:0',
            'width:' + COL_WIDTH + 'px',
            'height:100vh',
            'background:' + t.colBg,
            'border-left:2px solid ' + t.colBorder,
            'box-shadow:-2px 0 16px ' + t.borderGlow,
            'z-index:500',
            'display:flex',
            'flex-direction:column',
            'overflow:hidden',
        ].join(';');

        const label = document.createElement('div');
        label.style.cssText = [
            'padding:6px 0 5px',
            'text-align:center',
            'font-family:' + t.font,
            'font-size:0.42rem',
            'letter-spacing:2px',
            'color:' + t.labelColor,
            'border-bottom:1px solid ' + t.divider,
            'flex-shrink:0',
            'text-transform:uppercase',
        ].join(';');
        label.textContent = t.label;
        col.appendChild(label);

        const closeBtn = document.createElement('div');
        closeBtn.style.cssText = [
            'position:absolute',
            'top:4px',
            'right:8px',
            'cursor:pointer',
            'font-size:0.7rem',
            'color:' + t.labelColor,
            'z-index:10',
            'opacity:0.7',
            'line-height:1',
        ].join(';');
        closeBtn.textContent = '✕';
        closeBtn.title = 'Close ads';
        closeBtn.addEventListener('click', function() {
            if (rafId) cancelAnimationFrame(rafId);
            col.remove();
            const main = document.querySelector('.main-content');
            if (main) main.style.marginRight = '';
            localStorage.setItem('adsClosed', Date.now().toString());
        });
        col.appendChild(closeBtn);

        const wrapper = document.createElement('div');
        wrapper.style.cssText = 'flex:1;overflow:hidden;position:relative;';
        const track = document.createElement('div');
        track.id = 'slink-ad-track';
        track.style.cssText = 'position:absolute;top:0;left:0;width:100%;padding-top:14px;';

        [ADS, ADS, ADS, ADS, ADS, ADS, ADS].forEach(function(set) {
            set.forEach(function(ad) {
                track.appendChild(buildCard(ad, t));
            });
        });

        wrapper.appendChild(track);
        col.appendChild(wrapper);

        return { col: col, track: track };
    }

    function startScroll() {
        if (rafId) cancelAnimationFrame(rafId);
        trackH = getTrackH();
        if (trackH === 0) {
            rafId = requestAnimationFrame(startScroll);
            return;
        }
        scrollY = parseFloat(localStorage.getItem('adScrollY') || '0') % trackH;
        trackEl.style.transform = 'translateY(-' + scrollY + 'px)';
        colEl.style.visibility = 'visible';

        function tick() {
            scrollY += SCROLL_SPEED;
            if (scrollY >= trackH) scrollY -= trackH;
            trackEl.style.transform = 'translateY(-' + scrollY + 'px)';
            rafId = requestAnimationFrame(tick);
        }
        rafId = requestAnimationFrame(tick);
    }

    function getTrackH() {
        const h = trackEl.scrollHeight / 3;
        return h > 10 ? h : 0;
    }

    function adjustLayout() {
        const main = document.querySelector('.main-content');
        if (main) main.style.marginRight = (COL_WIDTH + 8) + 'px';
    }

    function init() {
        const closedAt = localStorage.getItem('adsClosed');
        if (closedAt && (Date.now() - parseInt(closedAt)) < 1) return;
        localStorage.removeItem('adsClosed');
        const built = buildColumn();
        colEl   = built.col;
        trackEl = built.track;
        colEl.style.visibility = 'hidden';
        document.body.appendChild(colEl);
        adjustLayout();

        window.addEventListener('beforeunload', function() {
            localStorage.setItem('adScrollY', scrollY);
        });

        startScroll();

        function recheckTheme() {
            const current = isNeon();
            if (typeof recheckTheme._wasNeon === 'undefined') {
                recheckTheme._wasNeon = current;
                return;
            }
            if (recheckTheme._wasNeon !== current) {
                recheckTheme._wasNeon = current;
                if (rafId) cancelAnimationFrame(rafId);
                colEl.remove();
                scrollY = 0;
                const b = buildColumn();
                colEl   = b.col;
                trackEl = b.track;
                document.body.appendChild(colEl);
                adjustLayout();
                requestAnimationFrame(function() {
                    requestAnimationFrame(startScroll);
                });
            }
        }

        setInterval(recheckTheme, 300);

        window.addEventListener('storage', function(e) {
            if (e.key === 'theme') recheckTheme();
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();