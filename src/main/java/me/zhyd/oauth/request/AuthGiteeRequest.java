package me.zhyd.oauth.request;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.model.AuthUserGender;
import me.zhyd.oauth.url.GiteeUrlBuilder;
import me.zhyd.oauth.url.entity.AuthAccessTokenEntity;
import me.zhyd.oauth.url.entity.AuthAuthorizeEntity;
import me.zhyd.oauth.url.entity.AuthUserInfoEntity;

/**
 * Gitee登录
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @version 1.0
 * @since 1.8
 */
public class AuthGiteeRequest extends BaseAuthRequest {

    public AuthGiteeRequest(AuthConfig config) {
        super(config, AuthSource.GITEE, new GiteeUrlBuilder());
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        String accessTokenUrl = this.urlBuilder.getAccessTokenUrl(AuthAccessTokenEntity.builder()
                .config(config)
                .code(authCallback.getCode())
                .build());
        HttpResponse response = HttpRequest.post(accessTokenUrl).execute();
        JSONObject accessTokenObject = JSONObject.parseObject(response.body());
        if (accessTokenObject.containsKey("error")) {
            throw new AuthException("Unable to get token from gitee using code [" + authCallback.getCode() + "]");
        }
        return AuthToken.builder().accessToken(accessTokenObject.getString("access_token")).build();
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        String accessToken = authToken.getAccessToken();
        HttpResponse response = HttpRequest.get(this.urlBuilder.getUserInfoUrl(AuthUserInfoEntity.builder()
                .accessToken(accessToken)
                .build())).execute();
        String userInfo = response.body();
        JSONObject object = JSONObject.parseObject(userInfo);
        return AuthUser.builder()
                .uuid(object.getString("id"))
                .username(object.getString("login"))
                .avatar(object.getString("avatar_url"))
                .blog(object.getString("blog"))
                .nickname(object.getString("name"))
                .company(object.getString("company"))
                .location(object.getString("address"))
                .email(object.getString("email"))
                .remark(object.getString("bio"))
                .gender(AuthUserGender.UNKNOW)
                .token(authToken)
                .source(AuthSource.GITEE)
                .build();
    }

    /**
     * 返回认证url，可自行跳转页面
     *
     * @return 返回授权地址
     */
    @Override
    public String authorize() {
        return this.urlBuilder.getAuthorizeUrl(AuthAuthorizeEntity.builder()
                .config(config)
                .build());
    }
}
