package ua.com.danit.servlet;

import ua.com.danit.dto.User;
import ua.com.danit.service.ServiceLikes;
import ua.com.danit.service.ServiceUsers;
import ua.com.danit.utils.CookieUtil;
import ua.com.danit.utils.CryptUtil;
import ua.com.danit.utils.Freemarker;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

public class ServletUsers extends HttpServlet {

    private ServiceUsers serviceUsers;
    private ServiceLikes serviceLikes;
    private Freemarker freemarker = new Freemarker();
    private int countNext = 1;
    private int idUserFromCookie;

    public ServletUsers(Connection dbConn) {
        this.serviceUsers = new ServiceUsers(dbConn);
        this.serviceLikes = new ServiceLikes(dbConn);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        idUserFromCookie = new CookieUtil().getIdUser(req.getCookies());
        String gender = serviceUsers.getUser(idUserFromCookie).getGender();
        HashMap<String, Object> data = new HashMap<>();

        //забрати іфи!!! переробити counter
        int maxIdUser = serviceUsers.maxIdUser();
        User user = serviceUsers.getUser(countNext);

        if (countNext > maxIdUser) {
            countNext = 0;
            resp.sendRedirect("/liked");
        }
        while (user == null || gender.equals(user.getGender())) {
            if (countNext > maxIdUser) {
                countNext = 0;
                resp.sendRedirect("/liked");
            }
            countNext++;
            user = serviceUsers.getUser(countNext);
        }

        data.put("user", user);
        data.put("disliked", CryptUtil.encryptExtra("disliked"));
        data.put("liked", CryptUtil.encryptExtra("liked"));
        data.put("userId", CryptUtil.encryptExtra(Integer.toString(user.getId())));

        freemarker.render("like-page.ftl", data, resp);
        countNext++;
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        idUserFromCookie = new CookieUtil().getIdUser(req.getCookies());

        String action = CryptUtil.decryptPrmName(req);
        int idUserWhom = CryptUtil.decryptPrmValue(req);

        if(action.equals("disliked")){
            if (serviceLikes.checkLike(idUserFromCookie, idUserWhom)){
                serviceLikes.delLike(idUserFromCookie, idUserWhom);
                doGet(req,resp);
            }else {
                doGet(req, resp);
            }
        }
        if(action.equals("liked")){
            if(serviceLikes.checkLike(idUserFromCookie, idUserWhom)){
                doGet(req,resp);
            }else{
                serviceLikes.saveLike(idUserFromCookie, idUserWhom);
                doGet(req,resp);
            }
        }

//        String dislike = req.getParameter("disliked");
//        String like = req.getParameter("liked");
//        int userIdWhom;
//
//        //remove if
//        if(like != null){
//            userIdWhom = Integer.parseInt(like);
//            if(serviceLikes.checkLike(idUserFromCookie, userIdWhom)){
//                doGet(req,resp);
//            }else{
//                serviceLikes.saveLike(idUserFromCookie, userIdWhom);
//                doGet(req,resp);
//            }
//        }else if(dislike != null){
//            userIdWhom = Integer.parseInt(dislike);
//            if (serviceLikes.checkLike(idUserFromCookie, userIdWhom)){
//                serviceLikes.delLike(idUserFromCookie, userIdWhom);
//                doGet(req,resp);
//            } else {
//                doGet(req,resp);
//            }
//        }
    }

}
