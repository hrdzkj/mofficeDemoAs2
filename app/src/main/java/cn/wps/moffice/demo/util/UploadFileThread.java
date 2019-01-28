package cn.wps.moffice.demo.util;

import java.io.ByteArrayOutputStream;

/**
 * Created by LiuYi on 2019/1/25.
 */
public class UploadFileThread extends Thread  {

   private ByteArrayOutputStream mOutputStream;
    public UploadFileThread(ByteArrayOutputStream outputStream)
    {
        super();
        mOutputStream = new ByteArrayOutputStream();
        if (outputStream!=null){
            byte[] bytes =outputStream.toByteArray();
            outputStream.write(bytes,0,bytes.length);
        }

    }

    @Override
    public void run() {
        super.run();
        uploadFile();
    }

    public void uploadFile() {
        // FileUtil.updateFile2(mOutputStream);
        FileUtil.saveFile(mOutputStream,"saveFile.doc");
    }


}
