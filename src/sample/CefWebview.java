package sample;

import com.jogamp.opengl.awt.GLJPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefRequestContext;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefRequestContextHandler;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookieManager;
import org.cef.network.CefRequest;
import org.cef.network.CefWebPluginInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class CefWebview {
    private Stage mStage;
    private Window owner;
    private AnchorPane cefPane;
    Parent root;
    CefBrowser browser;
    CefApp app;
    CefClient client;
    private static final String CEF_PATH = "L:\\data\\cef";

    public void start(Stage stage) throws Exception {
        initView(stage);
        initCef();
    }

    private void initView(Stage stage) {
        this.mStage = stage;
        try {
            root = loadLayout("./res/layout/login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        cefPane = (AnchorPane) root.lookup("#pane_cef");
        cefPane.setStyle("-fx-background-color: #EEEEEE");
        ImageView imgBack = (ImageView) root.lookup("#imageView_back");
        imgBack.setOnMousePressed((EventHandler<Event>) event -> {
            onBack();
        });
        Scene scene = new Scene(root);
        if (owner != null) {
            mStage.initOwner(owner);
        }
        mStage.setScene(scene);
        if (owner != null) {
            mStage.setX(owner.getX());
            mStage.setY(owner.getY());
        }
        mStage.show();
    }

    private void initCef() {
        try {
            String libPath = System.getProperty("java.library.path");
            if (!libPath.contains(CEF_PATH)) {
                addLibraryDir(CEF_PATH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingNode swingNode = new SwingNode();
        app = CefApp.getInstance();
        client = app.createClient();
        MyReqContextHandler contextHandler = new MyReqContextHandler(); // clear cookies
        CefRequestContext requestContext = CefRequestContext.createContext(contextHandler);
        browser = client.createBrowser("www.baidu.com", true, false, requestContext);
        GLJPanel browserUi = (GLJPanel) browser.getUIComponent();
        cefPane.getChildren().add(swingNode);
        AnchorPane.setTopAnchor(swingNode, 0d);
        AnchorPane.setBottomAnchor(swingNode, 0d);
        AnchorPane.setRightAnchor(swingNode, 0d);
        AnchorPane.setLeftAnchor(swingNode, 0d);
        swingNode.setContent(browserUi);

        client.addLoadHandler(new CefLoadHandler() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
            }

            @Override
            public void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {

            }

            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
            }
        });
    }

    private void onBack() {
        client.dispose();
        mStage.close();

    }

    public class MyReqContextHandler implements CefRequestContextHandler {
        CefCookieManager cookieManager = null;

        MyReqContextHandler() {
//            cookieManager = CefCookieManager.createManager("", false); // TODO can not clear cookie
        }

        public CefCookieManager getCookieManager() {
            return cookieManager;
        }

        @Override
        public boolean onBeforePluginLoad(String mime_type, String plugin_url, boolean is_main_frame, String top_origin_url, CefWebPluginInfo plugin_info) {
            return false;
        }

        @Override
        public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, BoolRef disableDefaultHandling) {
            return null;
        }
    }

    private  <T> T loadLayout(String res) throws IOException {
        T page = FXMLLoader.load(getClass().getResource(res));
        return page;
    }

    private static void addLibraryDir(String libraryPath) throws Exception {
        Field userPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        userPathsField.setAccessible(true);
        String[] paths = (String[]) userPathsField.get(null);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            if (libraryPath.equals(paths[i])) {
                continue;
            }
            sb.append(paths[i]).append(File.pathSeparatorChar);
        }
        sb.append(libraryPath);
        System.setProperty("java.library.path", sb.toString());
        final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
        sysPathsField.setAccessible(true);
        sysPathsField.set(null, null);
    }


}
