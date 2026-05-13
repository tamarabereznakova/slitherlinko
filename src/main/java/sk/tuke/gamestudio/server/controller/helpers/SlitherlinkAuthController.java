package sk.tuke.gamestudio.server.controller.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.playerADDons.PlayerSettings;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.PlayerService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.playerADDons.FollowService;
import sk.tuke.gamestudio.service.entity.playerADDons.PlayerSettingsService;
import sk.tuke.gamestudio.service.entity.playerADDons.SpectateService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class SlitherlinkAuthController {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerSettingsService playerSettingsService;
    @Autowired
    SpectateService spectateService;
    @Autowired
    private FollowService followService;

    @RequestMapping("/home")
    public String index(
            @RequestParam(value = "returning", required = false) String returningParam,
            @RequestParam(value = "error", required = false) String error,
            javax.servlet.http.HttpServletRequest request,
            Model model) {

        if (session.getPlayerName() != null) return "redirect:/slitherlink/level";
        model.addAttribute("error", error);
        model.addAttribute("returningParam", returningParam);
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "index";
    }

    @RequestMapping("/slitherlink/rules")
    public String rules(Model model) {
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "rules";
    }

    @PostMapping("/login")
    public String login(@RequestParam(value = "returning") String returningParam, @RequestParam(value = "name") String name, @RequestParam(value = "password") String password, @RequestParam(value = "spectate", required = false) String spectateToken) {

        final String trimmedName = name.trim();
        final boolean isReturning = returningParam.equals("yes");

        if (trimmedName.isEmpty() || trimmedName.length() > 10)
            return "redirect:/home?returning=" + returningParam + "&error=invalidname";
        if (password == null || password.length() < 4 || password.length() > 10)
            return "redirect:/home?returning=" + returningParam + "&error=invalidpassword";

        if (isReturning) return handleReturningPlayer(trimmedName, password, spectateToken);
        else return handleNewPlayer(trimmedName, password, spectateToken);
    }

    private String handleReturningPlayer(String name, String password, String spectateToken) {
        if (!playerService.login(name, password)) {
            if (!playerService.exists(name)) return "redirect:/home?returning=yes&error=notfound";
            return "redirect:/home?returning=yes&error=wrongpassword";
        }
        session.setReturning(true);
        session.setHasPlayed(true);
        session.setGamesPlayed((int) scoreService.getTopScores("slitherlink").stream().filter(s -> s.getPlayer().equals(name)).count());
        session.setPlayerName(name);
        session.setSettings(playerSettingsService.getSettings(name));
        if (spectateToken != null && !spectateToken.isBlank())
            return "redirect:/spectate/" + spectateToken;
        return "redirect:/slitherlink/level";
    }

    private String handleNewPlayer(String name, String password, String spectateToken) {
        if (playerService.exists(name)) return "redirect:/home?returning=no&error=exists";
        if (!playerService.register(name, password)) return "redirect:/home?returning=no&error=exists";
        session.setReturning(false);
        session.setHasPlayed(false);
        session.setGamesPlayed(0);
        session.setPlayerName(name);
        session.setSettings(new PlayerSettings(name));
        if (spectateToken != null && !spectateToken.isBlank())
            return "redirect:/spectate/" + spectateToken;
        return "redirect:/slitherlink/level";
    }

    @RequestMapping("/logout")
    public String logout() {
        if (session.getPlayerName() != null) {
            spectateService.deleteByPlayer(session.getPlayerName());
        }
        session.setPlayerName(null);
        session.setPlayerName(null);
        session.setReturning(false);
        session.setHasPlayed(false);
        session.setGamesPlayed(0);
        session.setField(null);
        session.setPlay(null);
        session.setSettings(null);
        return "redirect:/home";
    }

    @GetMapping("/slitherlink/checkname")
    @ResponseBody
    public boolean checkName(@RequestParam String name, @RequestParam String returning) {
        return playerService.exists(name.trim());
    }
}