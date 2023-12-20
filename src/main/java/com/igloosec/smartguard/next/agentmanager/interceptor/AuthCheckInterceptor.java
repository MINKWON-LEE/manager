package com.igloosec.smartguard.next.agentmanager.interceptor;

import com.igloosec.smartguard.next.agentmanager.api.model.SmartGuardToken;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.exception.UnAuthorizationException;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class AuthCheckInterceptor extends HandlerInterceptorAdapter {

    private Dao dao;
    SmartGuardToken sgToken;


    public AuthCheckInterceptor(Dao dao) throws Exception {

        this.dao = dao;
        sgToken = new SmartGuardToken();
    }

    /**
     * WAS->MANAGER HEADER [WAS@@USER_ID@@MANAGERCD) | 암호화
     *     ex) AES128Utility.aes_encrypt("WAS@@USER_ID@@MANAGERCD")
     *         55865d03293789ff19ece7b3ddc6ced0e7c22701f1e9b2e9bd5dbf69049ff7d2
     * AGENT->MANAGER HEADER [AGENT@@HOSTNAME@@IP@@VERSION) | 암호화
     *     ex) AES128Utility.aes_encrypt("AGENT@@vision@@192.168.80.110@@1.1.2")
     *         44d3ce3c1f9c547a041d3c6315bc4bbab3d2b63e67b80596f1ad9088b77bd7d655e993e03c6206585ebb32a4fb802669
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authroization = request.getHeader("Authorization");
        // log.info("authroization - {}" , authroization);

        if(StringUtil.isEmpty(authroization)) throw new UnAuthorizationException("header is empty.");

        //authorization check.
        try {
            // sgToken = new SmartGuardToken(dao, authroization);
            if (!sgToken.isValid(authroization)) {
                log.error("unauthorized header : " + authroization);
                throw new UnAuthorizationException("unauthorized header.");
            }
        }catch (Exception e){
            log.error("exception header : " + authroization);
            throw new UnAuthorizationException(e.getMessage());
        }

        return super.preHandle(request, response, handler);
    }
}
