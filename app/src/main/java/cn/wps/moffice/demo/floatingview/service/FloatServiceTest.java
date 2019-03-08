package cn.wps.moffice.demo.floatingview.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import cn.wps.moffice.client.AllowChangeCallBack;
import cn.wps.moffice.demo.MyApplication;
import cn.wps.moffice.demo.R;
import cn.wps.moffice.demo.floatingview.FloatingFunc;
import cn.wps.moffice.demo.util.Define;
import cn.wps.moffice.demo.util.FileUtil;
import cn.wps.moffice.demo.util.ToastUtil;
import cn.wps.moffice.demo.util.Util;
import cn.wps.moffice.service.OfficeService;
import cn.wps.moffice.service.base.print.PrintOutItem;
import cn.wps.moffice.service.base.print.PrintProgress;
import cn.wps.moffice.service.doc.Document;
import cn.wps.moffice.service.doc.PictureFormat;
import cn.wps.moffice.service.doc.ProtectionType;
import cn.wps.moffice.service.doc.SaveFormat;
import cn.wps.moffice.service.doc.WrapType;
import cn.wps.moffice.service.pdf.PDFReader;
import cn.wps.moffice.service.presentation.Presentation;
import cn.wps.moffice.service.spreadsheet.Workbook;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FloatServiceTest extends Service implements OnClickListener {

    private final static String WINDOW_NOTE = "按住该按钮可以拖动窗口\n文件名为：";
    final int QUALITY = 100;
    final int DPI = 64;
    final int PRINT_ITEM = PrintOutItem.wdPrintContent;

    private static Context mContext;     //上一级Context 为了关闭浮动窗口
    private static Context myContext;   //自身Context 为了关闭service
    public static boolean isBound = false; //是否绑定,为了在关闭wps接收到广播后解绑
    public static boolean isChangedFlag = false;
    public boolean isProtected = false;
    private static AllowChangeCallBack mAllow;
    private static String docPath = "/sdcard/DCIM/文档9.doc";

    private OfficeService mService;
    private static Document mDoc = null;
    private static PDFReader mPdfReader = null;
    private static Presentation mPresentation = null;
    private static Workbook mWorkBook = null;

    private View view;

    private static TextView txt_fileName;
    private Button btnHideAll;
    private View allBtnGroup;
    private Button btnCloseWindow;
    private Button btnNewDocument;
    private Button btnOpenFile;
    private Button btnCloseFile;
    private Button btnGetPath;
    private Button btnGetPageCount;
    private Button btnSave;
    private Button btnSaveAs;
    private Button btnHiddenToolBar;
    private Button btnHiddenMenuBar;
    private Button btnIsModified;
    private Button btnHandWriteComment;
    private Button btnChangeReviseMode;
    private Button btnAcceptAllRevision;
    private Button btnUndo;
    private Button btnRedo;
    private Button btnClearAllComments;
    private Button btnGetCurrentPageNum;
    private Button btnSaveCurrentPageToImage;
    private Button btnSaveAllPageToImage;
    private Button btnGetLength;
    private Button btnGetScrollY;
    private Button btnGetScrollX;
    private Button btnAddDocumentVariable;
    private Button btnGetDocumentVariable;
    private Button btnReadOnlyProtect;
    private Button btnCommentProtect;
    private Button btnTrackedProtect;
    private Button btnUnProtect;
    private Button btnIsProtectOn;
    private Button btnGetProtectionType;
    private Button btnToggleInkFinger;
    private Button btnToggleToPen;
    private Button btnToggleToHighLightPen;
    private Button btnToggleToEraser;
    private Button btnSetInkColor;
    private Button btnGetInkColor;
    private Button btnGetInkPenThick;
    private Button btnGetInkHighLightThick;
    private Button btnSetInkThick;

    //以下是选区的aidl按钮
    private Button btnCopy;
    private Button btnCut;
    private Button btnPaste;
    private Button btnTypeText;
    private Button btnGetText;
    private Button btnInsertParagraph;
    private Button btnGetLeft;
    private Button btnGetTop;
    private Button btnGetStart;
    private Button btnGetEnd;
    private Button btnSetSelection;
    private Button btngetFont;

    //InlineShape
    private Button btnAddInlinePicture;
    //Shapes
    private Button btnAddPicture;
    private Button btnAddTextBox;
    private Button btnAddNoneTextBox;

    //选区字体相关设置
    private Button btnRemoveStrikeThrough;
    private Button btnSetStrikeThrough;

    // 上下翻页
    private Button btnPrePage;
    private Button btnNextPage;
    private Button btnGetShapeLeft;
    private Button btnGetShapeTop;
    private Button btnGetShapeHeight;
    private Button btnGetShapeWidth;
    private Button btnGetTextBoxLineColor;
    private Button btnGetTextBoxFLine;


    private boolean isSharedEnable = true;

    private boolean mIsHindeAll = false;
    private boolean mShowHandWriteComment = false;
    private Boolean mEnterRevisionMode = false;
    private final static String NEW_DOCUMENT_PATH = "/sdcard/专业版新建文档.doc";
    private final static String SAVE_AS_PATH = "/sdcard/专业版另存文档.doc";
    private final static String SAVE_CURRUNT_PAGE = "/sdcard/专业版截图.png";//截图保存路径
    private final static String SAVE_IMAGE_PATH = "/sdcard/pic";
    private final static String INLINE_PIC_PATH = "/sdcard/DCIM/ico.png";

    private final static int NEW_DOCUMENT = 0;
    private final static int OPEN_DOCUMENT = 1;

    private final static String FAIL_NOTE = "操作失败！";
    private final static String VARIABLE_EXIST = "该属性已经存在，不能重复添加！";
    private final static String INSERT_STR = "这是插入的内容！";        //插入到选区的内容

    //文档保护类型
    public final static short TRACKEDCHANGES = 0;
    public final static short COMMENTS = 1;
    public final static short READONLY = 3;
    public final static short NONE = 7;

    private final static String PROTECT_PASSWORD = "123";

    private boolean mIsbindService = false;

    @Override
    public void onCreate() {
        Log.d("FloatingService", "onCreate");
        super.onCreate();
        mContext = getApplicationContext();
        myContext = this;
        view = LayoutInflater.from(this).inflate(R.layout.float_test_view, null);

        findViewByID();
        createView();
        // bindOfficeService();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mDoc = null;
        FloatingFunc.show(this.getApplicationContext(), view);
        File file = new File(docPath);
        txt_fileName.setText(WINDOW_NOTE + "\n" + file.getName());
        if (mIsbindService && mService == null) {

        }

        if (mService == null) {
            /*
            if (connection!=null) { // 绑定了服务但是未连接
                try {
                    unbindService(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            */
            mIsbindService = bindOfficeService();
        } else {
            ToastUtil.showShort(" 服务已经绑定");
        }

        return START_NOT_STICKY;
    }


    private void findViewByID() {
        txt_fileName = (TextView) view.findViewById(R.id.filename);
        btnHideAll = (Button) view.findViewById(R.id.btnHideAll);
        allBtnGroup = view.findViewById(R.id.allbtngroup);

        btnCloseWindow = (Button) view.findViewById(R.id.btnCloseWindow);
        btnNewDocument = (Button) view.findViewById(R.id.btnNewDocument);
        btnOpenFile = (Button) view.findViewById(R.id.btnOpenFile);
        btnCloseFile = (Button) view.findViewById(R.id.btnCloseFile);
        btnGetPath = (Button) view.findViewById(R.id.btnGetPath);
        btnGetPageCount = (Button) view.findViewById(R.id.btnGetPageCount);
        btnSave = (Button) view.findViewById(R.id.btnSave);
        btnSaveAs = (Button) view.findViewById(R.id.btnSaveAs);
        btnHiddenToolBar = (Button) view.findViewById(R.id.btnHiddenToolBar);
        btnHiddenMenuBar = (Button) view.findViewById(R.id.btnHiddenMenuBar);
        btnIsModified = (Button) view.findViewById(R.id.btnIsModified);
        btnHandWriteComment = (Button) view.findViewById(R.id.btnHandWriteComment);
        btnChangeReviseMode = (Button) view.findViewById(R.id.btnChangeReviseMode);
        btnAcceptAllRevision = (Button) view.findViewById(R.id.btnAcceptAllRevision);
        btnUndo = (Button) view.findViewById(R.id.btnUndo);
        btnRedo = (Button) view.findViewById(R.id.btnRedo);
        btnClearAllComments = (Button) view.findViewById(R.id.btnClearAllComments);
        btnGetCurrentPageNum = (Button) view.findViewById(R.id.btnGetCurrentPageNum);
        btnSaveCurrentPageToImage = (Button) view.findViewById(R.id.btnSaveCurrentPageToImage);
        btnSaveAllPageToImage = (Button) view.findViewById(R.id.btnSaveAllPageToImage);
        btnGetLength = (Button) view.findViewById(R.id.btnGetLength);
        btnGetScrollY = (Button) view.findViewById(R.id.btnGetScrollY);
        btnGetScrollX = (Button) view.findViewById(R.id.btnGetScrollX);
        btnAddDocumentVariable = (Button) view.findViewById(R.id.btnAddDocumentVariable);
        btnGetDocumentVariable = (Button) view.findViewById(R.id.btnGetDocumentVariable);
        btnReadOnlyProtect = (Button) view.findViewById(R.id.btnReadOnlyProtect);
        btnCommentProtect = (Button) view.findViewById(R.id.btnCommentProtect);
        btnTrackedProtect = (Button) view.findViewById(R.id.btnTrackedProtect);
        btnUnProtect = (Button) view.findViewById(R.id.btnUnProtect);
        btnIsProtectOn = (Button) view.findViewById(R.id.btnIsProtectOn);
        btnGetProtectionType = (Button) view.findViewById(R.id.btnGetProtectionType);
        btnToggleInkFinger = (Button) view.findViewById(R.id.btnToggleInkFinger);
        btnToggleToPen = (Button) view.findViewById(R.id.btnToggleToPen);
        btnToggleToHighLightPen = (Button) view.findViewById(R.id.btnToggleToHighLightPen);
        btnToggleToEraser = (Button) view.findViewById(R.id.btnToggleToEraser);
        btnSetInkColor = (Button) view.findViewById(R.id.btnSetInkColor);
        btnGetInkColor = (Button) view.findViewById(R.id.btnGetInkColor);
        btnGetInkPenThick = (Button) view.findViewById(R.id.btnGetInkPenThick);
        btnGetInkHighLightThick = (Button) view.findViewById(R.id.btnGetInkHighLightThick);
        btnSetInkThick = (Button) view.findViewById(R.id.btnSetInkThick);

        //以下是选区aidl操作的按钮
        btnCopy = (Button) view.findViewById(R.id.btnCopy);
        btnCut = (Button) view.findViewById(R.id.btnCut);
        btnPaste = (Button) view.findViewById(R.id.btnPaste);
        btnTypeText = (Button) view.findViewById(R.id.btnTypeText);
        btnGetText = (Button) view.findViewById(R.id.btnGetText);
        btnInsertParagraph = (Button) view.findViewById(R.id.btnInsertParagraph);
        btnGetLeft = (Button) view.findViewById(R.id.btnGetLeft);
        btnGetTop = (Button) view.findViewById(R.id.btnGetTop);
        btnGetStart = (Button) view.findViewById(R.id.btnGetStart);
        btnGetEnd = (Button) view.findViewById(R.id.btnGetEnd);
        btnSetSelection = (Button) view.findViewById(R.id.btnSetSelection);
        btngetFont = (Button) view.findViewById(R.id.btngetFont);

        //InlineShape
        btnAddInlinePicture = (Button) view.findViewById(R.id.btnAddInlinePicture);
        //Shapes
        btnAddPicture = (Button) view.findViewById(R.id.btnAddPicture);
        btnAddTextBox = (Button) view.findViewById(R.id.btnAddTextBox);
        btnAddNoneTextBox = (Button) view.findViewById(R.id.btnAddNoneTextBox);

        //font
        btnRemoveStrikeThrough = (Button) view.findViewById(R.id.btnRemoveStrikeThrough);
        btnSetStrikeThrough = (Button) view.findViewById(R.id.btnSetStrikeThrough);

        // turn page
        btnPrePage = (Button) view.findViewById(R.id.btnPrePage);
        btnNextPage = (Button) view.findViewById(R.id.btnNextPage);

        //shape 相关属性
        btnGetShapeLeft = (Button) view.findViewById(R.id.btnGetShapeLeft);
        btnGetShapeTop = (Button) view.findViewById(R.id.btnGetShapeTop);
        btnGetShapeHeight = (Button) view.findViewById(R.id.btnGetShapeHeight);
        btnGetShapeWidth = (Button) view.findViewById(R.id.btnGetShapeWidth);
        btnGetTextBoxLineColor = (Button) view.findViewById(R.id.btnGetTextBoxLineColor);
        btnGetTextBoxFLine = (Button) view.findViewById(R.id.btnGetTextBoxFLine);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void createView() {
        File file = new File(docPath);
        txt_fileName.setText(WINDOW_NOTE + "\n" + file.getName());

        btnHideAll.setOnClickListener(this);
        btnCloseWindow.setOnClickListener(this);
        btnNewDocument.setOnClickListener(this);
        btnOpenFile.setOnClickListener(this);
        btnCloseFile.setOnClickListener(this);
        btnGetPath.setOnClickListener(this);
        btnGetPageCount.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnSaveAs.setOnClickListener(this);
        btnHiddenToolBar.setOnClickListener(this);
        btnHiddenMenuBar.setOnClickListener(this);
        btnIsModified.setOnClickListener(this);
        btnHandWriteComment.setOnClickListener(this);
        btnChangeReviseMode.setOnClickListener(this);
        btnAcceptAllRevision.setOnClickListener(this);
        btnUndo.setOnClickListener(this);
        btnRedo.setOnClickListener(this);
        btnClearAllComments.setOnClickListener(this);
        btnGetCurrentPageNum.setOnClickListener(this);
        btnSaveCurrentPageToImage.setOnClickListener(this);
        btnSaveAllPageToImage.setOnClickListener(this);
        btnGetLength.setOnClickListener(this);
        btnGetScrollY.setOnClickListener(this);
        btnGetScrollX.setOnClickListener(this);
        btnAddDocumentVariable.setOnClickListener(this);
        btnGetDocumentVariable.setOnClickListener(this);
        btnReadOnlyProtect.setOnClickListener(this);
        btnCommentProtect.setOnClickListener(this);
        btnTrackedProtect.setOnClickListener(this);
        btnUnProtect.setOnClickListener(this);
        btnIsProtectOn.setOnClickListener(this);
        btnGetProtectionType.setOnClickListener(this);
        btnToggleInkFinger.setOnClickListener(this);
        btnToggleToPen.setOnClickListener(this);
        btnToggleToHighLightPen.setOnClickListener(this);
        btnToggleToEraser.setOnClickListener(this);
        btnSetInkColor.setOnClickListener(this);
        btnGetInkColor.setOnClickListener(this);
        btnGetInkPenThick.setOnClickListener(this);
        btnGetInkHighLightThick.setOnClickListener(this);
        btnSetInkThick.setOnClickListener(this);

        //下面是选区操作
        btnCopy.setOnClickListener(this);
        btnCut.setOnClickListener(this);
        btnPaste.setOnClickListener(this);
        btnTypeText.setOnClickListener(this);
        btnGetText.setOnClickListener(this);
        btnInsertParagraph.setOnClickListener(this);
        btnGetLeft.setOnClickListener(this);
        btnGetTop.setOnClickListener(this);
        btnGetStart.setOnClickListener(this);
        btnGetEnd.setOnClickListener(this);
        btnSetSelection.setOnClickListener(this);
        btngetFont.setOnClickListener(this);

        //InlineShape
        btnAddInlinePicture.setOnClickListener(this);
        //Shapes
        btnAddPicture.setOnClickListener(this);
        btnAddTextBox.setOnClickListener(this);
        btnAddNoneTextBox.setOnClickListener(this);

        //font
        btnRemoveStrikeThrough.setOnClickListener(this);
        btnSetStrikeThrough.setOnClickListener(this);

        btnPrePage.setOnClickListener(this);
        btnNextPage.setOnClickListener(this);

        btnGetShapeLeft.setOnClickListener(this);
        btnGetShapeTop.setOnClickListener(this);
        btnGetShapeHeight.setOnClickListener(this);
        btnGetShapeWidth.setOnClickListener(this);
        btnGetTextBoxLineColor.setOnClickListener(this);
        btnGetTextBoxFLine.setOnClickListener(this);

        txt_fileName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                FloatingFunc.onTouchEvent(arg1, txt_fileName);
                return true;
            }
        });
    }

    protected Disposable mDisposable;
    protected void unsubscribe() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnHideAll:
                hideAll();
                break;
            case R.id.btnCloseWindow:
                this.stopSelf();
                break;
            case R.id.btnNewDocument:
                newDocment();
                break;
            case R.id.btnOpenFile:
                if (Util.isPDFFile(docPath)) {
                    openPDFDoc();
                } else if (Util.isPptFile(docPath)) {
                    openPresentation();
                } else if (Util.isExcelFile(docPath)) {
                    openWorkBook();
                } else {
                    //openDocument();
                    unsubscribe();
                    mDisposable= FileUtil.downLoadFile(FileUtil.testDowloadFile)
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aBoolean -> {
                                if (aBoolean.booleanValue()) {
                                    ToastUtil.showShort("开始打开文件");
                                    openDocument();
                                    //ToastUtil.showShort("下载文件成功");
                                }else{
                                    ToastUtil.showShort("下载文件异常");
                                }
                            }, throwable -> {
                                throwable.printStackTrace();
                                ToastUtil.showShort("网络异常");
                            });
                }
                break;
            case R.id.btnCloseFile:
                if (Util.isPDFFile(docPath)) {
                    closePDFDoc();
                } else if (Util.isPptFile(docPath)) {
                    closePresentation();
                } else if (Util.isExcelFile(docPath)) {
                    closeWorkBook();
                } else {
                    closeFile();
                }
                break;
            case R.id.btnGetPath:
                getPath();
                break;
            case R.id.btnGetPageCount:
                getPageCount();
                break;
            case R.id.btnSave:
                saveDocument();
                break;
            case R.id.btnSaveAs:
                saveAsDocument();
                break;
            case R.id.btnHiddenToolBar:
                hiddenToolBar();
                break;
            case R.id.btnHiddenMenuBar:
                hiddenMenuBar();
                break;
            case R.id.btnIsModified:
                isModified();
                break;
            case R.id.btnHandWriteComment:
                handWriteComment();
                break;
//		case 清稿
            case R.id.btnChangeReviseMode:
                changeRevisionMode();
                break;
            case R.id.btnAcceptAllRevision:
                acceptAllRevision();
                break;
            case R.id.btnUndo:
                undo();
                break;
            case R.id.btnRedo:
                redo();
                break;
            case R.id.btnClearAllComments:
                clearAllComments();
                break;
            case R.id.btnGetCurrentPageNum:
                getCurrentPageNum();
                break;
            case R.id.btnSaveCurrentPageToImage:
                saveCurrentPageToImage();
                break;
            case R.id.btnSaveAllPageToImage:
                saveAllPageToImage();
                break;
            case R.id.btnGetLength:
                getLength();
                break;
            case R.id.btnGetScrollY:
                getScrollY();
                break;
            case R.id.btnGetScrollX:
                getScrollX();
                break;

            //以下是选区相关操作
            case R.id.btnCopy:
                copy();
                break;
            case R.id.btnCut:
                cut();
                break;
            case R.id.btnPaste:
                paste();
                break;
            case R.id.btnTypeText:
                typeText();
                break;
            case R.id.btnGetText:
                getText();
                break;
            case R.id.btnInsertParagraph:
                insertParagraph();
                break;
            case R.id.btnGetLeft:
                getLeft();
                break;
            case R.id.btnGetTop:
                getTop();
                break;
            case R.id.btnGetStart:
                getStart();
                break;
            case R.id.btnGetEnd:
                getEnd();
                break;
            case R.id.btnSetSelection:
                setSelection();
                break;
            case R.id.btngetFont:
                getFont();
                break;
            //InlineShape
            case R.id.btnAddInlinePicture:
                addInlinePicture();
                break;
            //Shapes
            case R.id.btnAddPicture:
                addPicture();
                break;
            case R.id.btnAddTextBox:
                addTextBox();
                break;
            case R.id.btnAddNoneTextBox:
                addNoneTextBox();
                break;
            case R.id.btnAddDocumentVariable:
                addDocumentVariable();
                break;
            case R.id.btnGetDocumentVariable:
                getDocumentVariable();
                break;
            case R.id.btnReadOnlyProtect:
                protectDocument(READONLY);
                break;
            case R.id.btnCommentProtect:
                protectDocument(COMMENTS);
                break;
            case R.id.btnTrackedProtect:
                protectDocument(TRACKEDCHANGES);
                break;
            case R.id.btnUnProtect:
                unProtectDocument(PROTECT_PASSWORD);
                break;
            case R.id.btnIsProtectOn:
                isProtectOn();
                break;
            case R.id.btnGetProtectionType:
                getProtectionType();
                break;
            case R.id.btnRemoveStrikeThrough:
                removeStrikeThrough();
                break;
            case R.id.btnSetStrikeThrough:
                setStrikeThrough();
                break;
            case R.id.btnPrePage:
                turnPrePage();
                break;
            case R.id.btnNextPage:
                turnNextPage();
                break;
            case R.id.btnGetShapeLeft:
                getShapeLeft();
                break;
            case R.id.btnGetShapeTop:
                getShapeTop();
                break;
            case R.id.btnGetShapeHeight:
                getShapeHeight();
                break;
            case R.id.btnGetShapeWidth:
                getShapeWidth();
                break;
            case R.id.btnGetTextBoxLineColor:
                getTextBoxLineColor();
                break;
            case R.id.btnGetTextBoxFLine:
                getTextBoxFLine();
                break;
            case R.id.btnToggleInkFinger:
                toggleInkFinger();
                break;
            case R.id.btnToggleToPen:
                toggleToPen();
                break;
            case R.id.btnToggleToHighLightPen:
                toggleToHighLightPen();
                break;
            case R.id.btnToggleToEraser:
                toggleToEraser();
                break;
            case R.id.btnSetInkColor:
                setInkColor();
                break;
            case R.id.btnGetInkColor:
                getInkColor();
                break;
            case R.id.btnGetInkPenThick:
                getInkPenThick();
                break;
            case R.id.btnGetInkHighLightThick:
                getInkHighLightThick();
                break;
            case R.id.btnSetInkThick:
                setInkThick();
                break;
            default:
                break;
        }
    }

    // 新建文档
    private void newDocment() {
        if (mService == null) return;

        if (null != mDoc) {
            Util.showToast(mContext, "文档已经打开，请先关闭文档再新建！");
            return;
        }

        File file = new File(NEW_DOCUMENT_PATH);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Util.showToast(mContext, "新建文档失败！");
            return;
        }
        LoadDocThread mythreadNew = new LoadDocThread(NEW_DOCUMENT_PATH, NEW_DOCUMENT);
        mythreadNew.start();
    }

    // 打开文档
    private void openDocument() {
        if (mService == null) {
            Util.showToast(mContext, "操作失败，无正常的service!");
            return;
        }

        LoadDocThread mythread = new LoadDocThread(docPath, OPEN_DOCUMENT);
        mythread.start();
    }

    //关闭文档
    private void closeFile() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.close();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //文档路径
    private void getPath() {
        if (!isDocumentOpened() && !isPDFDocOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            String path = Util.isPDFFile(docPath) ? mPdfReader.getPath() : mDoc.getPath();
            Util.showToast(mContext, path);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //文档页数
    private void getPageCount() {
        if (!isDocumentOpened() && !isPresentationOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int pageCount = mDoc.getPageCount();
            String message = "文档页数为 ： " + pageCount;
            Util.showToast(mContext, message);
            return;
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        } catch (Throwable e) {

        }

        try {
            int pageCount = mPresentation.getPageCount();
            String message = "文档页数为 ： " + pageCount;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        } catch (Throwable e) {

        }
    }

    //保存文档
    private void saveDocument() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.save(false);
            Util.showToast(mContext, "保存成功");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //另存文档
    private void saveAsDocument() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.saveAs(SAVE_AS_PATH, SaveFormat.DOC, "", "");

            Util.showToast(mContext, "另存成功，另存路径为 ： " + SAVE_AS_PATH);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //隐藏工具栏
    private void hiddenToolBar() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.hiddenToolBar();
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //隐藏菜单栏
    private void hiddenMenuBar() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.hiddenMenuBar();
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //是否修改
    private void isModified() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            boolean isModified = mDoc.isModified();
            String message = "是否修改  ： " + isModified;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //操作手绘
    private void handWriteComment() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            if (mShowHandWriteComment) {
                mDoc.closeHandWriteComment();
                mShowHandWriteComment = false;
                btnHandWriteComment.setText("打开手绘");
            } else {
                mDoc.showHandWriteComment();
                mShowHandWriteComment = true;
                btnHandWriteComment.setText("关闭手绘");
            }
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //进入或退出修订模式
    private void changeRevisionMode() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            if (mEnterRevisionMode) {
                mDoc.exitReviseMode();
                mEnterRevisionMode = false;
                btnChangeReviseMode.setText("进入修订模式");
            } else {
                mDoc.enterReviseMode();
                mEnterRevisionMode = true;
                btnChangeReviseMode.setText("退出修订模式");
            }
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //接受所有修订
    private void clearAllComments() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.clearAllComments();
            Util.showToast(mContext, "删除所有批注成功");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //撤销
    private void undo() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.undo();
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //还原
    private void redo() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.redo();
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //接受所有修订
    private void acceptAllRevision() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.acceptAllRevision();
            Util.showToast(mContext, "接受所有修订成功");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //当前页页码
    private void getCurrentPageNum() {
        if (!isDocumentOpened() && !isPDFDocOpened() && !isPresentationOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int number = Util.isPDFFile(docPath) ? mPdfReader.getCurrentPageNum() : mDoc.getCurrentPageNum(0);   //参数0没有什么意义，为后期功能兼容保留的
            String message = "当前页页码为  ： " + number;
            Util.showToast(mContext, message);
            return;
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        } catch (Throwable e) {

        }

        try {
            int number = mPresentation.getCurrentPageIndex();   //参数0没有什么意义，为后期功能兼容保留的
            String message = "当前页页码为  ： " + number;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        } catch (Throwable e) {

        }
    }

    //截图当前页
    private void saveCurrentPageToImage() {
        if (!isDocumentOpened() && !isPresentationOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.saveCurrentPageToImage(SAVE_CURRUNT_PAGE, PictureFormat.PNG, 0, 0, PrintOutItem.wdPrintContent, Color.WHITE, 1190, 1682);
            Util.showToast(mContext, "截图成功，图片已经保存到 ： " + SAVE_CURRUNT_PAGE);
            return;
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        } catch (Throwable e) {

        }

        try {
            String result = mPresentation.exportCurPageToImage(SAVE_IMAGE_PATH, PictureFormat.PNG);
            Util.showToast(mContext, "截图成功，图片已经保存到 ： " + result);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        } catch (Throwable e) {

        }
    }

    private void saveAllPageToImage() {
        if (!isPresentationOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            List<String> paths = mPresentation.exportImage(SAVE_IMAGE_PATH, PictureFormat.PNG, new PrintProgress() {

                @Override
                public IBinder asBinder() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public boolean isCanceled() throws RemoteException {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public void exportProgress(int progress) throws RemoteException {
                    // TODO Auto-generated method stub

                }
            });
            Util.showToast(mContext, paths.toString());
        } catch (RemoteException e) {

        }
    }

    //文档长度
    private void getLength() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int length = mDoc.getLength();
            String message = "文档长度为  ： " + length;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //当前文档已经划过的Y长度
    private void getScrollY() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            float y = mDoc.getScrollY();
            String message = "已经划过的y长度  ： " + y;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //当前文档已经划过的Y长度
    private void getScrollX() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            float x = mDoc.getScrollX();
            String message = "已经划过的x长度  ： " + x;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //添加文档属性
    private void addDocumentVariable() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        addDocumentVariableDialog();
        Util.showToast(mContext, "插入成功");
    }

    //获得文档属性
    private void getDocumentVariable() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        getDocumentVariableDialog();
    }

    //文档保护
    private void protectDocument(int protectionType) {
        try {
            if (mDoc.isProtectOn()) {
                Util.showToast(mContext, "文档已经被保护！");
                return;
            }

            mDoc.protect(PROTECT_PASSWORD, protectionType, true);
            Util.showToast(mContext, "设置文档保护成功！");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //取消文档保护
    private void unProtectDocument(String password) {
        try {
            if (!mDoc.isProtectOn()) {
                Util.showToast(mContext, "文档未加保护！");
                return;
            }

            mDoc.unprotect(password);
            Util.showToast(mContext, "文档解保护成功！");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //文档是否保护
    private void isProtectOn() {
        try {
            boolean isProtect = mDoc.isProtectOn();
            String message = "文档是否保护 ： " + isProtect;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //文档保护类型
    private void getProtectionType() {
        try {
            int protectionType = mDoc.getProtectionType();
            String message;

            switch (protectionType) {
                case ProtectionType.NONE:
                    message = "文档保护模式 ： 无保护";
                    break;
                case ProtectionType.COMMENTS:
                    message = "文档保护模式 ： 批注保护";
                    break;
                case ProtectionType.TRACKEDCHANGES:
                    message = "文档保护模式 ： 修订保护";
                    break;
                case ProtectionType.READONLY:
                    message = "文档保护模式 ： 制度保护";
                    break;
                default:
                    message = "文档保护模式 ： 无保护";
                    break;
            }

            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //复制 选区
    private void copy() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.getSelection().copy();
            Util.showToast(mContext, "选区内容已经复制到剪切板");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //剪切 选区
    private void cut() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.getSelection().cut();
            Util.showToast(mContext, "选区内容已经剪切到剪切板");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //粘贴 选区
    private void paste() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.getSelection().paste();
            Util.showToast(mContext, "粘贴成功");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //插入文字到 选区
    private void typeText() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.getSelection().typeText(INSERT_STR);
            Util.showToast(mContext, "插入文字成功！");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //获得 选区文字
    private void getText() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            String content = mDoc.getSelection().getText();
            String message = "选区内容为  ： " + content;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //插入一个段
    private void insertParagraph() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.getSelection().insertParagraph();
            Util.showToast(mContext, "插入段成功");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //选区左边界坐标
    private void getLeft() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            float left = mDoc.getSelection().getLeft();
            String message = "选区左边界坐标 ： " + left;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //选区上边界坐标
    private void getTop() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            float top = mDoc.getSelection().getTop();
            String message = "选区上边界坐标 ： " + top;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //选区开始 cp位置
    private void getStart() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int start = mDoc.getSelection().getStart();
            String message = "选区开始cp ： " + start;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //选区结束 cp位置
    private void getEnd() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int end = mDoc.getSelection().getEnd();
            String message = "选区结束cp ： " + end;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //设置选区
    private void setSelection() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            setSelectionDialog(mDoc.getLength());
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //选区字体信息
    private void getFont() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            String name = mDoc.getSelection().getFont().getName();
            float size = mDoc.getSelection().getFont().getSize();
            int color = mDoc.getSelection().getFont().getTextColor();
            boolean bold = mDoc.getSelection().getFont().getBold();
            boolean italic = mDoc.getSelection().getFont().getItalic();

            String message = "字体名称 ： " + name + "字体大小 ： " + size + "字体颜色 ： " + color + "是否粗体 ： " + bold + "是否斜体： " + italic;
            Util.showToast(mContext, message);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //添加嵌入式的图片到选区
    private void addInlinePicture() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        if (!new File(INLINE_PIC_PATH).exists()) {
            Util.showToast(mContext, "图片不存在！图片路径应为:/sdcard/DCIM/ico.png");
            return;
        }

        try {
            mDoc.getSelection().getInlineShapes().addPicture(INLINE_PIC_PATH);
            Util.showToast(mContext, "插入嵌入图成功");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //添加浮动图
    private void addPicture() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        addPictureDialog();
    }

    //添加文本框
    private void addTextBox() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int width = (int) mDoc.getSelection().getLeft() - mDoc.getScrollX();
            int height = (int) mDoc.getSelection().getTop() - mDoc.getScrollY();
            mDoc.getShapes().addTextBox(width, height, 80, 40, 0x00ff0000, false, 0x0000ff00, false, 0x0000ff, (float) 20.5, "宋体", "通过啦！！！");
            Util.showToast(mContext, "插入文本框成功");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    //添加空文本框
    private void addNoneTextBox() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int width = (int) mDoc.getSelection().getLeft() - mDoc.getScrollX();
            int height = (int) mDoc.getSelection().getTop() - mDoc.getScrollY();
            mDoc.getShapes().addTextBox(width, height, 80, 40, 0x00ff0000, false, 0x0000ff00, false, 0x0000ff, (float) 20.5, "宋体", "");
            Util.showToast(mContext, "插入空文本框成功");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void removeStrikeThrough() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.getSelection().getFont().setNoneStrikeThrough();
            Util.showToast(mContext, "去掉删除线成功！");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void setStrikeThrough() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.getSelection().getFont().setStrikeThrough();
            Util.showToast(mContext, "添加删除线成功！");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void toggleInkFinger() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.toggleInkFinger();
            Util.showToast(mContext, "使用手指成功！");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void toggleToPen() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.toggleToPen();
            Util.showToast(mContext, "切换笔成功！");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void toggleToHighLightPen() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.toggleToHighLightPen();
            Util.showToast(mContext, "切换荧光笔成功！");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void toggleToEraser() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.toggleToEraser();
            Util.showToast(mContext, "切换橡皮擦成功！");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void setInkColor() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.setInkColor(0xff0000);
            Util.showToast(mContext, "设置墨迹颜色成功！已设置为红色");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void getInkColor() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int color = mDoc.getInkColor();
            Util.showToast(mContext, "墨迹颜色为：" + color);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void getInkPenThick() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            float inkPenThick = mDoc.getInkPenThick();
            Util.showToast(mContext, "笔的粗细为 ： " + inkPenThick);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void getInkHighLightThick() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            float highLightThick = mDoc.getInkHighLightThick();
            Util.showToast(mContext, "荧光笔的粗细为 ： " + highLightThick);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private void setInkThick() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mDoc.setInkThick((float) 5.0);
            Util.showToast(mContext, "设置笔的粗细成功，笔的粗细为 5.0磅");
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    private boolean isDocumentOpened() {
        return null != mDoc;
    }

    //隐藏所有按钮
    private void hideAll() {
        if (mIsHindeAll) {
            mIsHindeAll = false;
            btnHideAll.setText("隐藏所有");
            allBtnGroup.setVisibility(View.VISIBLE);
        } else {
            mIsHindeAll = true;
            btnHideAll.setText("展开所有");
            allBtnGroup.setVisibility(View.GONE);
        }
    }


    /**
     * 停止服务调用
     */
    public static void stopService() {
        Log.i("FloatingService", "btnCloseWindow");
        FloatingFunc.close(mContext);
        isBound = false;
        mDoc = null;
        mPdfReader = null;
        mPresentation = null;
        ((Service) myContext).stopSelf();//关闭自身service
    }

    @Override
    public void onDestroy() {
        Log.d("FloatingService", "onDestroy");
        unsubscribe();

        FloatingFunc.close(this.getApplicationContext());
        if (connection != null) unbindService(connection);

        mDoc = null;
        mPdfReader = null;
        mPresentation = null;
        this.stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 处理为绑定死亡
     */
    private void doBindDeath(){
        mService = null;
        isBound = false;
        mIsbindService =false;
    }


    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {                  // 当绑定的service异常断开连接后，会自动执行此方法
            doBindDeath();

            /*
            if (mIMyAidlInterface != null){
                mIMyAidlInterface.asBinder().unlinkToDeath(mDeathRecipient, 0);
                bindService(new Intent("com.service.bind"),mMyServiceConnection,BIND_AUTO_CREATE);      //  重新绑定服务端的service
            }
            */
        }
    };

    /**
     * connection of binding
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = OfficeService.Stub.asInterface(service);
            try { // 注册死亡代理
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            doBindDeath();
        }
    };

    // bind service
    private boolean bindOfficeService() {
        isChangedFlag = false;

        final Intent intent = new Intent(Define.OFFICE_SERVICE_ACTION);
        intent.putExtra("DisplayView", true);
        intent.setPackage(Define.PACKAGENAME_KING_PRO);
        boolean isbindService = false;
        try {
            isbindService = MyApplication.getInstance().bindService(intent, connection, Service.BIND_AUTO_CREATE);
        } catch (SecurityException e) {
            e.printStackTrace();
        } finally {
            ToastUtil.showShort("isbindService=" + isbindService);
            Log.v("isbindService=", "" + isbindService);
        }
        return isbindService;
    }

    // 设置文档路径
    public static void setDocPath(String path) {
        docPath = path;
    }

    public static Document getDocument() {
        return mDoc;
    }

    public static void setAllowCallBack(AllowChangeCallBack allow) {
        mAllow = allow;
    }

    public void turnPrePage() {
        if (!isDocumentOpened() && !isPDFDocOpened() && !isPresentationOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            if (mPdfReader != null) {
                mPdfReader.transitionPre();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            if (mPresentation != null) mPresentation.transitionPre();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void turnNextPage() {
        if (!isDocumentOpened() && !isPDFDocOpened() && !isPresentationOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            if (mPdfReader != null) {
                mPdfReader.transitionNext();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            if (mPresentation != null) mPresentation.transitionNext();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getShapeLeft() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int left = (int) mDoc.getShapes().getPicLeft();
            Util.showToast(mContext, "对象左边缘坐标为： " + left);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    public void getShapeTop() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int top = (int) mDoc.getShapes().getPicTop();
            Util.showToast(mContext, "对象上边缘坐标为： " + top);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    public void getShapeHeight() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int height = (int) mDoc.getShapes().getPicHeight();
            Util.showToast(mContext, "对象高度 为： " + height);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    public void getShapeWidth() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int width = (int) mDoc.getShapes().getPicWidth();
            Util.showToast(mContext, "对象宽度为： " + width);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    public void getTextBoxLineColor() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            int color = (int) mDoc.getShapes().getTextBoxLineColor();
            Util.showToast(mContext, "文本框边框颜色为： " + color);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    public void getTextBoxFLine() {
        if (!isDocumentOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            boolean fLine = mDoc.getShapes().getTextBoxFLine();
            Util.showToast(mContext, "文本框边框是否显示： " + fLine);
        } catch (RemoteException e) {
            Util.showToast(mContext, FAIL_NOTE);
            e.printStackTrace();
        }
    }

    class LoadDocThread extends Thread// 内部类
    {
        String path;
        int flag;

        public LoadDocThread(String path, int openFlag) {
            this.path = path;
            this.flag = openFlag;
        }

        public void run() {
            // 打开文档
            if (mService == null ) {
                Util.showToast(mContext, "操作失败，service可能为正常连接");
                return;
            }

            try {
                Intent intent = Util.getOpenIntent(mContext, this.path, true);
                //intent.getExtras();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Define.USER_NAME, "LiuYi");
               // intent.putExtra(Define.IS_CLEAR_TRACE,true);
                intent.putExtra(Define.THIRD_PACKAGE, getPackageName());
                if (OPEN_DOCUMENT == flag) {
                    mDoc = mService.openDocument(path, "", intent);
                } else if (NEW_DOCUMENT == flag) {
                    mDoc = mService.newDocument(path, intent);
                }
            } catch (RemoteException e) {
                mDoc = null;
                e.printStackTrace();
            }
        }
    }

    class LoadPDFDocThread extends Thread// 内部类
    {
        String path;

        public LoadPDFDocThread(String path) {
            this.path = path;
        }

        public void run() {
            // 打开文档
            if (mService == null ) {
                Util.showToast(mContext, "操作失败，service可能为正常连接");
                return;
            }

            try {
                Intent intent = Util.getPDFOpenIntent(mContext, this.path, true);
                mPdfReader = mService.openPDFReader(path, "", intent);
            } catch (RemoteException e) {
                mPdfReader = null;
                e.printStackTrace();
            }
        }
    }

    class LoadPresentationThread extends Thread {
        String mFilePath;

        public LoadPresentationThread(String filePath) {
            mFilePath = filePath;
        }

        public void run() {
            // 打开文档
            if (mService == null ) {
                Util.showToast(mContext, "操作失败，service可能为正常连接");
                return;
            }

            try {
                Intent intent = Util.getOpenIntent(mContext, mFilePath, true);
                mPresentation = mService.openPresentation(mFilePath, "", intent);
            } catch (RemoteException e) {
                mPresentation = null;
                e.printStackTrace();
            }
        }
    }

    class LoadWorkBookThread extends Thread {
        String mFilePath;

        public LoadWorkBookThread(String filePath) {
            mFilePath = filePath;
        }

        public void run() {
            // 打开文档
            if (mService == null ) {
                Util.showToast(mContext, "操作失败，service可能为正常连接");
                return;
            }

            try {
                Intent intent = Util.getOpenIntent(mContext, mFilePath, true);
                mWorkBook = mService.getWorkbooks().openBookEx(mFilePath, "", intent);
            } catch (RemoteException e) {
                mWorkBook = null;
                e.printStackTrace();
            }
        }
    }

    // 打开文档
    private void openPDFDoc() {
        if (mService == null) return;

        LoadPDFDocThread mythread = new LoadPDFDocThread(docPath);
        mythread.start();
    }

    private void openPresentation() {
        LoadPresentationThread loadThread = new LoadPresentationThread(docPath);
        loadThread.start();
    }

    private void openWorkBook() {
        if (mService == null) return;
        LoadWorkBookThread loadThread = new LoadWorkBookThread(docPath);
        loadThread.start();
    }

    private void closePDFDoc() {
        if (!isPDFDocOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mPdfReader.close();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void closeWorkBook() {
        if (!isWorkBookOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mWorkBook.close();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void closePresentation() {
        if (!isPresentationOpened()) {
            Util.showToast(mContext, "操作失败，文档未打开，请先打开或者新建文档");
            return;
        }

        try {
            mPresentation.close();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean isPDFDocOpened() {
        return null != mPdfReader;
    }

    private boolean isPresentationOpened() {
        return mPresentation != null;
    }

    private boolean isWorkBookOpened() {
        return mWorkBook != null;
    }

    //设置selection的位置 对话框
    private void setSelectionDialog(int length) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View ParamDialogView = inflater.inflate(R.layout.param_text_dialog, null);

        AlertDialog paramAlertDialog = new AlertDialog.Builder(mContext).setTitle("请输入光标的位置(不大于" + (length - 1) + ")").setView(ParamDialogView).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editAddText = (EditText) ParamDialogView.findViewById(R.id.editAddText);
                int pos = -1;
                String str = editAddText.getText().toString();
                if ("".equals(str) || str.length() == 0) {
                    Toast.makeText(mContext, "请输入数值", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    try {
                        pos = Integer.valueOf(str);
                    } catch (NumberFormatException e) {
                        Toast.makeText(mContext, "输入不是数字，请重新操作", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }
                }

                try {
                    mDoc.getSelection().setSelection(pos, pos, true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        paramAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        paramAlertDialog.show();
    }

    //插入浮动图片对话框, shape
    private void addPictureDialog() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View ParamDialogView = inflater.inflate(R.layout.shapes_dialog, null);
        EditText editFilePath = (EditText) ParamDialogView.findViewById(R.id.filePath);
        editFilePath.setText(INLINE_PIC_PATH);

        EditText editLeft = (EditText) ParamDialogView.findViewById(R.id.left);
        editLeft.setText(10 + "");

        EditText editTop = (EditText) ParamDialogView.findViewById(R.id.top);
        editTop.setText(10 + "");

        Bitmap bitmap = BitmapFactory.decodeFile(INLINE_PIC_PATH);

        EditText editWidth = (EditText) ParamDialogView.findViewById(R.id.width);
        editWidth.setText(bitmap.getWidth() + "");

        EditText editHeight = (EditText) ParamDialogView.findViewById(R.id.height);
        editHeight.setText(bitmap.getHeight() + "");

        EditText editCp = (EditText) ParamDialogView.findViewById(R.id.cp);
        editCp.setText(0 + "");


        AlertDialog paramAlertDialog = new AlertDialog.Builder(mContext).setTitle("设置图片属性").setView(ParamDialogView).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editFilePath = (EditText) ParamDialogView.findViewById(R.id.filePath);
                EditText editLeft = (EditText) ParamDialogView.findViewById(R.id.left);
                EditText editTop = (EditText) ParamDialogView.findViewById(R.id.top);
                EditText editWidth = (EditText) ParamDialogView.findViewById(R.id.width);
                EditText editHeight = (EditText) ParamDialogView.findViewById(R.id.height);
                EditText editCp = (EditText) ParamDialogView.findViewById(R.id.cp);

                RadioGroup raInkToFileGroup = (RadioGroup) ParamDialogView.findViewById(R.id.raInkToFileGroup);
                RadioButton raInkToFile = (RadioButton) ParamDialogView.findViewById(raInkToFileGroup.getCheckedRadioButtonId());

                RadioGroup raSaveWithDocumentGroup = (RadioGroup) ParamDialogView.findViewById(R.id.raSaveWithDocumentGroup);
                RadioButton raSaveWithDocument = (RadioButton) ParamDialogView.findViewById(raSaveWithDocumentGroup.getCheckedRadioButtonId());

                RadioGroup raWrapTypeGroup = (RadioGroup) ParamDialogView.findViewById(R.id.raWrapTypeGroup);
                RadioButton raWrapType = (RadioButton) ParamDialogView.findViewById(raWrapTypeGroup.getCheckedRadioButtonId());

                String filePath = editFilePath.getText().toString();
                String leftString = editLeft.getText().toString();
                String topString = editTop.getText().toString();
                String widthsString = editWidth.getText().toString();
                String heightsString = editHeight.getText().toString();
                String cpString = editCp.getText().toString();

                if (!new File(filePath).exists()) {
                    Util.showToast(mContext, "图片不存在!!");
                    return;
                }
                if ("".equals(leftString) || "".equals(topString) || "".equals(widthsString) || "".equals(heightsString) || "".equals(cpString)) {
                    Util.showToast(mContext, "请填写正确的参数!!");
                    return;
                }

                float left = Float.valueOf(leftString);
                float top = Float.valueOf(topString);
                float width = Float.valueOf(widthsString);
                float height = Float.valueOf(heightsString);
                int cp = Integer.valueOf(cpString);

                boolean inkToFile = Boolean.valueOf(raInkToFile.getText().toString());
                boolean saveWithDocument = Boolean.valueOf(raSaveWithDocument.getText().toString());
                String wrapType = raWrapType.getText().toString();

                try {
                    float mLeaf = mDoc.getSelection().getLeft();
                    float mTop = mDoc.getSelection().getTop();
                    int mCp = mDoc.getSelection().getStart(); //获取选区的开始位置，始终在选区的开始插入图片

                    mDoc.getShapes().addPicture(filePath, inkToFile, saveWithDocument, mLeaf, mTop, width, height, mCp, WrapType.valueOf(wrapType));

                } catch (RemoteException e) {
                    Toast.makeText(mContext, "插入失败!!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                Util.showToast(mContext, "插入图片成功!!");
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        paramAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        paramAlertDialog.show();
    }

    // 插入自定义属性的key value值
    private void addDocumentVariableDialog() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View ParamDialogView = inflater.inflate(R.layout.param_variable_dialog, null);

        AlertDialog paramAlertDialog = new AlertDialog.Builder(mContext).setTitle("插入文本内容").setView(ParamDialogView).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editAddText1 = (EditText) ParamDialogView.findViewById(R.id.editAddText1);

                EditText editAddText2 = (EditText) ParamDialogView.findViewById(R.id.editAddText2);

                if ("".equals(editAddText1.getText().toString()) || editAddText1.getText().toString().length() == 0) {
                    Util.showToast(mContext, "请输入文本");
                    return;
                }

                String key = editAddText1.getText().toString();
                String value = editAddText2.getText().toString();
                try {
                    mDoc.addDocumentVariable(key, value);
                    Util.showToast(mContext, "插入自定义属性 ：" + key + " : " + value + " 成功！");
                } catch (RemoteException e) {
                    Util.showToast(mContext, FAIL_NOTE);
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    Util.showToast(mContext, VARIABLE_EXIST);
                    e.printStackTrace();
                }

            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        paramAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        paramAlertDialog.show();
    }

    //弹出key值对应的文档属性
    private void getDocumentVariableDialog() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View ParamDialogView = inflater.inflate(R.layout.param_text_dialog, null);

        AlertDialog paramAlertDialog = new AlertDialog.Builder(mContext).setTitle("插入文本内容").setView(ParamDialogView).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editAddText = (EditText) ParamDialogView.findViewById(R.id.editAddText);
                if ("".equals(editAddText.getText().toString()) || editAddText.getText().toString().length() == 0) {
                    Util.showToast(mContext, "请输入文本");
                    return;
                }

                String key = editAddText.getText().toString();
                String value = "";
                try {
                    value = mDoc.getDocumentVariable(key);
                    Util.showToast(mContext, key + " 对应的value为: " + value);
                } catch (RemoteException e) {
                    Util.showToast(mContext, FAIL_NOTE);
                    e.printStackTrace();
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        paramAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        paramAlertDialog.show();
    }
}
