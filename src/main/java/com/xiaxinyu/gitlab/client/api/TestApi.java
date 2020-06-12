package com.xiaxinyu.gitlab.client.api;

import org.gitlab4j.api.GitLabApi.ApiVersion;
import org.gitlab4j.api.models.User;

import javax.naming.AuthenticationException;
import java.util.List;

/**
 * ClassName: TestApi
 *
 * @author dushaohua5
 * @Description: 测试
 * @date 2017年7月12日
 */
public class TestApi {

    String gitLabUrl = "http://gitlab.crc.com.cn";

    protected void authenticate(String username, String password) throws AuthenticationException {
        try {
            System.out.println("Trying to authenticate with username: " + username);
            org.gitlab4j.api.GitLabApi api = new org.gitlab4j.api.GitLabApi(ApiVersion.V4, gitLabUrl,
                    "Zzj5yMBz5BavyzgpbaJH");

            //for (int i = 0; i < 21; i++) {
                List<User> pro = api.getUserApi().getUsers(0,10);
                for (int j = 0; j < pro.size(); j++) {
                    System.out.println(pro.get(j).getExternUid());
                }
           // }

//            List<Project> list = api.getProjectApi().getProjects();
//            System.out.println("size: " + list.size());
//            for (int j = 0; j < list.size(); j++) {
//                List<ProjectHook> hooks = api.getProjectApi().getHooks(list.get(j).getId());
//                for (int k = 0; k < hooks.size(); k++) {
//                    ProjectHook hook = hooks.get(k);
//                    if (hook.getUrl().contains("api.devops.crc.com.cn")) {
//                        System.out.println(list.get(j).getPath() + "~~~" + list.get(j).getName() + "~~~" + hooks.get(k).getUrl());
//                        hook.setUrl(hook.getUrl().replace("api.devops.crc.com.cn", "api.steam.crcloud.com"));
//                        api.getProjectApi().modifyHook(hook);
//                        break;
//                    }
//                }
//            }


            //user = api.getUserApi().getUser("dushaohua5");
            // System.out.println(JSON.toJSONString(user));
            // org.gitlab4j.api.GitLabApi oldApi = new org.gitlab4j.api.GitLabApi(ApiVersion.V4, "http://gitlabold.devops.crc.com.cn",
            //       "dd");


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Authentication request failed for username: " + username);
            throw new AuthenticationException("Unable to process authentication for username: " + username);
        }
    }

    public static void main(String[] args) {
        TestApi test = new TestApi();
        try {
            test.authenticate("root", "Mango!23");
            // test.authenticate("dushaohua5", "Xa7883fc");
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

}
