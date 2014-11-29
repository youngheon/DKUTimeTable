package dk.too.timetable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import dk.too.util.Util;

public class Parse {

    public static List<DKClass> getClasses(String schoolNumber, String password, Context context) throws Exception {

        trustAllHosts();

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

        // ============ Step1 ==================
        // param set
        HttpPost httpPost = new HttpPost("http://sso.dankook.ac.kr/sso/pmi-sso-login-uid-password2.html");

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("uid", schoolNumber)); // 32081926,
                                                                  // 32081929,
                                                                  // 32091956
        qparams.add(new BasicNameValuePair("password", password));
        qparams.add(new BasicNameValuePair("gid", "info"));
        qparams.add(new BasicNameValuePair("returl", "http://daninfo.dankook.ac.kr/sso/login.aspx"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(qparams, "UTF-8");
        httpPost.setEntity(entity);


        // ============ Step2 ==================
        // login check
        HttpResponse response = httpclient.execute(httpPost);
        InputStream loginStream = response.getEntity().getContent();
        try {
            String loginHtml = writeHtml(loginStream);
            if (!loginCheck(loginHtml))
                throw new IOException("로그인 실패");
        } finally {
            loginStream.close();
        }

        
        // ============ Step3 ==================
        // parsing        
        // 강의 시간표 : http://daninfo.dankook.ac.kr/hagsa/hla/hla_sigan.asp
        // 수강확인서 출력 : http://daninfo.dankook.ac.kr/hagsa/hla/hla_confirm.asp
        HttpGet httpGet = new HttpGet("http://daninfo.dankook.ac.kr/hagsa/hla/hla_confirm.asp");
        response = httpclient.execute(httpGet);

        InputStream in = response.getEntity().getContent();
        try {
            String html = writeHtml(in);
            
            return parseJericho(html, context);
        } finally {
            in.close();
        }        
        
    }

    private static String writeHtml(InputStream in) throws IOException {

        String htmlPath = Util.getExtPath() + "/tmp/dku_tt.html";

        new File(Util.getExtPath() + "/tmp/").mkdirs();
        new File(htmlPath).delete();

        FileOutputStream out = new FileOutputStream(htmlPath, false);

        byte[] buffer = new byte[4096];

        int readSize;
        while ((readSize = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, readSize);

            Log.d(Debug.D + "Parse", "debug. " + new String(buffer, 0, readSize, "EUC-KR"));
        }

        out.close();

        return htmlPath;
    }
    
    private static void debugHeader(HttpResponse response)
    {
        // debug
        Log.d(Debug.D + "Parse", "statusline. " + response.getStatusLine());
        Header[] h = response.getAllHeaders();
        for (int i = 0; i < h.length; i++) {
            Log.d(Debug.D + "Parse", "header. " + h[i].getName() + " : " + h[i].getValue());
        }
        // debug
    }

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static InputStream getHtmlPageMobile(String schoolNumber, String password) throws IOException {
        HttpPost httpPost = new HttpPost("http://203.237.226.95:8080/mobile/login/login_ok.jsp");

        // 32081926, 1929
        // 32091956

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("userid", schoolNumber));
        qparams.add(new BasicNameValuePair("userpw", password));
        qparams.add(new BasicNameValuePair("returnUrl", "../m7/m7_c1.jsp"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(qparams, "UTF-8");
        httpPost.setEntity(entity);

        HttpClient httpclient = new DefaultHttpClient();

        HttpResponse response = httpclient.execute(httpPost);

        response = httpclient.execute(httpPost);

        // 파싱
        return response.getEntity().getContent();
    }

    private static boolean loginCheck(String filePath) throws UnsupportedEncodingException, IOException {

        FileInputStream in = new FileInputStream(filePath);
        Source source = new Source(new InputStreamReader(in, "EUC-KR"));
        in.close();

        if (source.getTextExtractor().toString().contains("입력하신 정보를 다시 확인해주세요."))
            return false;

        return true;
    }

    /**
     * @throws Exception
     *             수강 시간표 html 페이지를 파싱한다.
     * 
     * @param in
     * @return 수업 목록
     * @throws IOException
     * @throws
     */
    private static List<DKClass> parseJericho(String filePath, Context Context) throws Exception {

        List<DKClass> list = new ArrayList<DKClass>();

        try {
            FileInputStream in = new FileInputStream(filePath);
            Source source = new Source(new InputStreamReader(in, "EUC-KR"));
            in.close();

            Segment haggi = new Segment(source, source.getAllElements(HTMLElementName.BR).get(2).getEnd(), source
                    .getAllElements(HTMLElementName.BR).get(3).getBegin());

            String str = haggi.toString();
            Log.d(Debug.D + "Parse", "semester. [" + str + "]");
            // 2013 년도 1 학기
            Pattern ptn = Pattern.compile("([0-9]{4}) 년도  ([0-9]{1}) 학기");

            Matcher mt = ptn.matcher(str);
            if (mt.find()) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Context).edit();
                editor.putString("year", mt.group(1));
                editor.putString("semester", mt.group(2));
                editor.commit();
            }

            Element table = source.getAllElements(HTMLElementName.TABLE).get(1);

            List<Element> trs = table.getAllElements(HTMLElementName.TR);

            for (int i = 1; i < trs.size(); i++) {

                List<Element> tds = trs.get(i).getAllElements(HTMLElementName.TD);

                String code = tds.get(1).getTextExtractor().toString(); // 과목코드
                String lecture = tds.get(2).getTextExtractor().toString();

                String _class = tds.get(3).getTextExtractor().toString(); // 분반

                // .replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>",
                // ""); // 과목명

//                Element unit = tds.get(4); // 학점
                String timeRoom = tds.get(5).getTextExtractor().toString(); // 시간,강의실
                                                                            // 화8,9(자연305)/목3(자연516)
                String professor = tds.get(6).getTextExtractor().toString(); // 교강사

                DKClass dk = new DKClass(code, _class, lecture, timeRoom, professor, "");
                list.add(dk);

                Log.d(Debug.D + "Parse", "lecture. [" + dk.toString() + "]");
            }

        } catch (NullPointerException e) {
            // 서버 상태 이상? 재시도 필요.
            throw new Exception("서버 연결 실패(2). 로그인 정보를 확인하세요.", e);

        } catch (IOException e) {
            // 연결이 불안정
            throw new Exception("서버 연결 실패(3)", e);
        }

        return list;
    }

    /**
     * @throws Exception
     *             수강 시간표 html 페이지를 파싱한다.
     * 
     * @param in
     * @return 수업 목록
     * @throws IOException
     * @throws
     */
    private static List<DKClass> parseJerichoMobile(InputStream in, String[] semester) throws Exception {

        List<DKClass> list = new ArrayList<DKClass>();

        try {
            Source source = new Source(new InputStreamReader(in));
            source.fullSequentialParse();

            Element top = source.getFirstElementByClass("tb_style_01");
            if (!top.isEmpty()) {
                String str = top.getFirstElement("caption").getTextExtractor().toString();
                Log.d(Debug.D + "Parse", "semester. [" + str + "]");
                // [2011년 2학기 / 확정과목수 : 9 / 확정학점 : 20]

                Pattern ptn = Pattern.compile("([0-9]*)년 ([0-9])학기");

                Matcher mt = ptn.matcher(str);
                if (mt.find()) {

                    semester[0] = mt.group(1);
                    semester[1] = mt.group(2);

                    Log.d(Debug.D + "Parse", "find. [" + mt.group(1) + "-" + mt.group(2) + "]");
                }
            }

            Element contents = source.getElementById("table_contents_1");

            List<Element> trs = contents.getAllElements(HTMLElementName.TR);
            List<Element> tds;
            for (int i = 2; i < trs.size(); i += 2) {
                // 과목코드,분반,학점,교강사,재수강
                // 과목명,요일/시간/강의실

                tds = trs.get(i).getAllElements(HTMLElementName.TD);
                String code = tds.get(0).getTextExtractor().toString();
                String _class = tds.get(1).getTextExtractor().toString();
                String professor = tds.get(3).getTextExtractor().toString();

                tds = trs.get(i + 1).getAllElements(HTMLElementName.TD);
                String lecture = tds.get(0).getTextExtractor().toString();
                String timeRoom = tds.get(1).getTextExtractor().toString();

                DKClass dk = new DKClass(code, _class, lecture, timeRoom, professor, "");
                list.add(dk);

                Log.d(Debug.D + "Parse", "lecture. [" + dk.toString() + "]");
                // 월9,10,11(자연516)/금5,6,7(자연517)
                // [327380 1 네트워크프로그래밍 월6/수8,9(자연517) 조경산 ]
                // [366770 2 시스템분석및설계 월1,2/화4(자연517) 유해영 ]
                // [378260 1 영상정보처리 화8,9/목1(자연517) 구자영 ]
                // [382230 2 운영체제 월10,11,12(자연305) 조성제 ]
                // [452750 4 컴퓨터실험2 목2,3(2공522) 이상범 ]
                // [452810 1 컴퓨터시스템설계 화2/수2,3(2공524) 최상일 ]
                // [454230 1 문자의탄생과발전으로보는중국 월8,9(사회215) 장호득 ]
                // [457470 5 공학멘토링 최천원 ]
                // [464450 1 젊음과웰빙 수5,6(인문211) 박소영 ]

            }
        } catch (NullPointerException e) {
            // 서버 상태 이상? 재시도 필요.
            throw new Exception("서버 연결 실패(2). 로그인 정보를 확인하세요.", e);

        } catch (IOException e) {
            // 연결이 불안정
            throw new Exception("서버 연결 실패(3)", e);
        }

        return list;
    }

}