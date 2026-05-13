package sk.tuke.gamestudio.server.controller.helpers;

import org.springframework.ui.Model;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.*;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.playerADDons.FollowService;
import java.util.List;

public class SlitherlinkControllerHelper {

    public static void addCommonAttributes(Model model, SlitherlinkSession session, ScoreService scoreService, CommentService commentService, RatingService ratingService, FollowService followService) {
        model.addAttribute("playerName", session.getPlayerName());
        model.addAttribute("hasPlayed", session.isHasPlayed());
        model.addAttribute("preferredTheme", session.getSettings() != null ? session.getSettings().getPreferredTheme() : null);
        model.addAttribute("scores0", scoreService.getTopScoresByLevel("slitherlink", 0));
        model.addAttribute("scores1", scoreService.getTopScoresByLevel("slitherlink", 1));
        model.addAttribute("scores2", scoreService.getTopScoresByLevel("slitherlink", 2));
        model.addAttribute("scores3", scoreService.getTopScoresByLevel("slitherlink", 3));
        model.addAttribute("comments", commentService.getComments("slitherlink"));
        model.addAttribute("avgRating", ratingService.getAverageRating("slitherlink"));
        model.addAttribute("myRating", session.getPlayerName() != null ? ratingService.getRating("slitherlink", session.getPlayerName()) : 0);
        final java.util.Set<String> followingNames = new java.util.HashSet<>();
        if (session.getPlayerName() != null && followService != null) {
            for (final sk.tuke.gamestudio.entity.playerADDons.Follow f :
                    followService.getFollowing(session.getPlayerName())) {
                followingNames.add(f.getFollowingName());
            }
        }
        model.addAttribute("followingNames", followingNames);
    }

    public static String getLevelName(int level) {
        switch (level) {
            case 0:
                return "2×2";
            case 2:
                return "7×7";
            case 3:
                return "10x10";
            default:
                return "5×5";
        }
    }

    public static String getHtmlField(Field field, boolean clickable) {
        if (field == null) return "";
        final StringBuilder sb = new StringBuilder();
        final int rows = field.getRows();
        final int cols = field.getCols();
        sb.append("<table class='slitherlink-grid'>");
        for (int r = 0; r <= rows; r++) {
            sb.append("<tr>");
            for (int c = 0; c <= cols; c++) {
                sb.append("<td class='dot'>•</td>");
                if (c < cols) {
                    final Edge e = field.getEdgeAt(field.getPoint(r, c), field.getPoint(r, c + 1));
                    final String css = resolveEdgeCss(e, "h-edge");
                    final String link = clickable ? buildEdgeUrl(r, c, r, c + 1) : "";
                    sb.append("<td class='").append(css).append("'>").append("<a href='").append(link).append("'></a></td>");
                }
            }
            sb.append("</tr>");
            if (r < rows) {
                sb.append("<tr>");
                for (int c = 0; c <= cols; c++) {
                    final Edge ve = field.getEdgeAt(field.getPoint(r, c), field.getPoint(r + 1, c));
                    final String vCss = resolveEdgeCss(ve, "v-edge");
                    final String link = clickable ? buildEdgeUrl(r, c, r + 1, c) : "";
                    sb.append("<td class='").append(vCss).append("'>").append("<a href='").append(link).append("'></a></td>");
                    if (c < cols) appendTileCell(sb, field.getTile(r, c));
                }
                sb.append("</tr>");
            }
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String resolveEdgeCss(Edge e, String base) {
        if (e != null && e.isActive()) return base + " active";
        if (e != null && e.getState() == EdgeState.MARKED_X) return base + " marked";
        return base;
    }

    private static void appendTileCell(StringBuilder sb, Tile t) {
        if (t instanceof Clue)
            sb.append("<td class='tile clue'>").append(((Clue) t).getValue()).append("</td>");
        else
            sb.append("<td class='tile'></td>");
    }

    private static String buildEdgeUrl(int r1, int c1, int r2, int c2) {
        if (r1 == r2) {
            final int tileRow = r1 - 1;
            final int tileCol = c1;
            if (tileRow < 0) return "/slitherlink?row=0&col=" + tileCol + "&side=T";
            else return "/slitherlink?row=" + tileRow + "&col=" + tileCol + "&side=B";
        } else {
            final int tileRow = r1;
            final int tileCol = c1 - 1;
            if (tileCol < 0) return "/slitherlink?row=" + tileRow + "&col=0&side=L";
            else return "/slitherlink?row=" + tileRow + "&col=" + tileCol + "&side=R";
        }
    }

    public static String serializeEdges(Field field) {
        final StringBuilder sb = new StringBuilder("[");
        for (final Edge e : field.getAllEdges()) {
            sb.append("\"").append(e.getState().name()).append("\",");
        }
        if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    public static void deserializeEdges(Field field, String json) {
        final String cleaned = json.replace("[", "").replace("]", "").replace("\"", "");
        final String[] states = cleaned.split(",");
        final List<Edge> edges = field.getAllEdges();
        for (int i = 0; i < edges.size() && i < states.length; i++) {
            edges.get(i).setState(EdgeState.valueOf(states[i]));
        }
    }
}