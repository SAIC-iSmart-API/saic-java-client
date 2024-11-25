package net.heberling.ismart.java.rest.api.v1;

/**
 * @author Doug Culnane
 */
public class OauthToken extends JsonResponseMessage {

  OauthTokenData data;
  Boolean IS_CHANGE_PASSWORD;
  String dept_id;
  Long expires_in;
  String account;

  public OauthTokenData getData() {
    return data;
  }

  public void setData(OauthTokenData data) {
    this.data = data;
  }

  public Boolean getIS_CHANGE_PASSWORD() {
    return IS_CHANGE_PASSWORD;
  }

  public void setIS_CHANGE_PASSWORD(Boolean iS_CHANGE_PASSWORD) {
    IS_CHANGE_PASSWORD = iS_CHANGE_PASSWORD;
  }

  public String getDept_id() {
    return dept_id;
  }

  public void setDept_id(String dept_id) {
    this.dept_id = dept_id;
  }

  public Long getExpires_in() {
    return expires_in;
  }

  public void setExpires_in(Long expires_in) {
    this.expires_in = expires_in;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public class OauthTokenData {

    public OauthTokenData() {}

    String tenant_id;
    String languageType;
    String user_name;
    String avatar;
    String token_type;
    String client_id;
    String access_token;
    String role_name;
    String refresh_token;
    String license;
    String post_id;
    String user_id;
    String role_id;
    String scope;
    String oauth_id;

    public String getTenant_id() {
      return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
      this.tenant_id = tenant_id;
    }

    public String getLanguageType() {
      return languageType;
    }

    public void setLanguageType(String languageType) {
      this.languageType = languageType;
    }

    public String getUser_name() {
      return user_name;
    }

    public void setUser_name(String user_name) {
      this.user_name = user_name;
    }

    public String getAvatar() {
      return avatar;
    }

    public void setAvatar(String avatar) {
      this.avatar = avatar;
    }

    public String getToken_type() {
      return token_type;
    }

    public void setToken_type(String token_type) {
      this.token_type = token_type;
    }

    public String getClient_id() {
      return client_id;
    }

    public void setClient_id(String client_id) {
      this.client_id = client_id;
    }

    public String getAccess_token() {
      return access_token;
    }

    public void setAccess_token(String access_token) {
      this.access_token = access_token;
    }

    public String getRole_name() {
      return role_name;
    }

    public void setRole_name(String role_name) {
      this.role_name = role_name;
    }

    public String getRefresh_token() {
      return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
      this.refresh_token = refresh_token;
    }

    public String getLicense() {
      return license;
    }

    public void setLicense(String license) {
      this.license = license;
    }

    public String getPost_id() {
      return post_id;
    }

    public void setPost_id(String post_id) {
      this.post_id = post_id;
    }

    public String getUser_id() {
      return user_id;
    }

    public void setUser_id(String user_id) {
      this.user_id = user_id;
    }

    public String getRole_id() {
      return role_id;
    }

    public void setRole_id(String role_id) {
      this.role_id = role_id;
    }

    public String getScope() {
      return scope;
    }

    public void setScope(String scope) {
      this.scope = scope;
    }

    public String getOauth_id() {
      return oauth_id;
    }

    public void setOauth_id(String oauth_id) {
      this.oauth_id = oauth_id;
    }
  }
}
