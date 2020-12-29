package com.efreight.afbase.component;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.eclipse.core.internal.registry.RegistryProviderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author lc
 * @date 2020/8/19 13:35
 */
@Slf4j
@Component
public class BirtComponent {

    private static IReportEngine iReportEngine = null;
    /**
     * 字体配置文件
     */
    private static final String FONT_CONFIG_FILE = "birt/fontsConfig.xml";

    /**
     * birt 设计文件的根路径
     */
    @Value("${birt.engine.rpt_file_base_path}")
    private String RPT_FILE_BASE_PATH;

    static {
        log.info("BIRT component init as " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        EngineConfig config = new EngineConfig();
        try {
            URL fontURL = BirtComponent.class.getClassLoader().getResource(FONT_CONFIG_FILE);
            config.setFontConfig(fontURL);
            Platform.startup(config);
            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            iReportEngine = factory.createReportEngine(config);
        } catch (BirtException e) {
            log.error(e.getMessage(), e);
        }
    }

    @PreDestroy
    private void destroy(){
        log.info("BIRT component destroy as " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        if(iReportEngine != null){
            try{
                iReportEngine.destroy();
                Platform.shutdown();
                RegistryProviderFactory.releaseDefault();
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
    }
    /**
     * 生成报告方法
     * @param rptFileName 设计文件名
     * @param renderOption 渲染选项
     * @param appContext 数据集
     * @throws Exception
     */
    public void generateReport(String rptFileName, IRenderOption renderOption, Map<String, Object> appContext) throws EngineException {
        String rptFilePath = Paths.get(RPT_FILE_BASE_PATH, rptFileName).toString();
        log.info("rpt design file path:" + rptFilePath);

        IReportRunnable iReportRunnable = iReportEngine.openReportDesign(rptFilePath);
        IRunAndRenderTask iRunAndRenderTask = iReportEngine.createRunAndRenderTask(iReportRunnable);
        iRunAndRenderTask.setAppContext(appContext);
        iRunAndRenderTask.setRenderOption(renderOption);
        iRunAndRenderTask.run();
        iRunAndRenderTask.close();
    }

    /**
     * 生成PDF格式数据存储到文件中
     * @param rptFileName
     * @param appContext
     */
    public void pdfReport(String rptFileName, Map<String, Object> appContext, File distFile) throws IOException, EngineException {
        if(distFile.exists()){
            throw new FileAlreadyExistsException(distFile.getAbsolutePath());
        }
        try(FileOutputStream fileOutputStream = new FileOutputStream(distFile)){
            pdfReport(rptFileName, appContext, fileOutputStream);
        }
    }

    /**
     * 生成PDF格式数据存储到输出流中
     * @param rptFileName
     * @param appContext
     * @param stream
     */
    public void pdfReport(String rptFileName, Map<String, Object> appContext, OutputStream stream) throws EngineException {
        PDFRenderOption pdfRenderOption = new PDFRenderOption();
        pdfRenderOption.setEmbededFont(true);
        pdfRenderOption.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
        pdfRenderOption.setOutputStream(stream);
        generateReport(rptFileName, pdfRenderOption, appContext);
    }

}
