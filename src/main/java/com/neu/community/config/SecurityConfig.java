package com.neu.community.config;

import com.neu.community.util.CommunityConstant;
import com.neu.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/resetPassword",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR,
                        AUTHORITY_USER
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/unTop",
                        "/discuss/wonderful",
                        "/discuss/unWonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                .and().csrf().disable();
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    //没有登陆
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            //如果期待返回XML则为异步请求
                            httpServletResponse.setContentType("application/plain;charset=utf8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录"));
                        }else{
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    //权限不足
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                        if(xRequestedWith.equals("XMLHttpRequest")){
                            //如果期待返回XML则为异步请求
                            httpServletResponse.setContentType("application/plain;charset=utf8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限"));
                        }else{
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/denied");
                        }
                    }
                });
        //覆盖logout拦截
        http.logout().logoutUrl("/securitylogout");
    }
}
