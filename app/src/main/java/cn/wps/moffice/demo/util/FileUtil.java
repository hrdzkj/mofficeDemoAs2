package cn.wps.moffice.demo.util;

import android.annotation.SuppressLint;
import android.os.RemoteException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import cn.wps.moffice.client.OfficeInputStream;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by LiuYi on 2019/1/24.
 */
public class FileUtil {

    public static String testDowloadFile = "http://10.203.5.91:6060/QZHBJOffice/attach/temp/382751/382751e6395f56-496e-41f5-ae88-33edbe4e38ed.doc";

    public static ByteArrayOutputStream mTempStream = new ByteArrayOutputStream();

    private static  OkHttpClient mOkHttpClient;
    private static  OkHttpClient getOkhttpClient()
    {
        if (mOkHttpClient==null){
            mOkHttpClient = new OkHttpClient.Builder().
                    readTimeout(30, TimeUnit.SECONDS).build();
        }
        return mOkHttpClient;
    }

    @SuppressLint("CheckResult")
    public static Observable<Boolean> downLoadFile(final String onlinePath) {
        return Observable.create(emitter -> {
            closeStream();
            mTempStream = new ByteArrayOutputStream();

            Request request = new Request.Builder().get().url(onlinePath).build();
            try {
                Response response = getOkhttpClient().newCall(request).execute();
                if (!response.isSuccessful()) {
                    emitter.onNext(false);
                    emitter.onComplete();
                    ToastUtil.showShort("连接异常");
                    return;
                }

                InputStream is;
                is = response.body().byteStream();
                try {
                    int len;
                    byte[] bytes = new byte[1024];
                    while ((len = is.read(bytes)) != -1) {
                        mTempStream.write(bytes, 0, len);
                    }
                    emitter.onNext(true);
                    emitter.onComplete();
                } catch (IOException oue) {
                    oue.printStackTrace();
                    closeStream();
                    emitter.onNext(false);
                    emitter.onComplete();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException isclose) {
                            isclose.printStackTrace();
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                emitter.onNext(false);
                emitter.onComplete();
            }
        });
    }

    // 参考共青团UploadWpsModifyFileThread.java 中的uploadFile
    @SuppressLint("CheckResult")
    public static Observable<Boolean> updateFile(OfficeInputStream input) {
        return Observable.create(emitter -> {
            try {
                String BOUNDARY = UUID.randomUUID().toString();  //随机生成边界
                //拼接参数
                String PREFIX = "--";
                String LINE_END = "\r\n";
                String fileName = "382751e6395f56-496e-41f5-ae88-33edbe4e38ed.doc";//"20190124.doc";
                String urlStr = "http://10.203.5.91:6060/QZHBJOffice/app/mobOffice/savedoc.html?userId=382751&fileName=attach/temp/382751/" + fileName + "&attachId=36304&po_moboffice_fn=" + fileName;


                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30 * 1000); //30秒连接超时
                connection.setReadTimeout(30 * 1000);   //30秒读取超时
                connection.setDoInput(true);  //允许文件输入流
                connection.setDoOutput(true); //允许文件输出流
                connection.setUseCaches(false);  //不允许使用缓存
                connection.setRequestMethod("POST");  //请求方式为POST
                connection.setRequestProperty("Connection", "keep-alive"); //保持连接
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY); //特别注意：Content-Type必须为multipart/form-data

                OutputStream outputSteam = connection.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputSteam);
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);

                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream" + LINE_END + LINE_END);
                dos.write(sb.toString().getBytes());

                //读取文件
                byte[] bytes = new byte[1024];
                int[] ints = new int[1024];
                int len = 0;

                while ((len = input.read(bytes, ints)) != -1) {
                    dos.write(bytes, 0, len);
                }
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();

                //获取返回码，根据返回码做相应处理
                int res = connection.getResponseCode();
                if (res == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                }
                emitter.onNext(true);
                emitter.onComplete();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                emitter.onNext(false);
                emitter.onComplete();
            } catch (IOException e) {
                e.printStackTrace();
                emitter.onNext(false);
                emitter.onComplete();
            } catch (RemoteException e) {
                e.printStackTrace();
                emitter.onNext(false);
                emitter.onComplete();
            }
        });


    }

    private static void closeStream() {
        if (mTempStream != null) {
            try {
                mTempStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
