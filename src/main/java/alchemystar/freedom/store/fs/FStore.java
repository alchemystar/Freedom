package alchemystar.freedom.store.fs;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.index.bp.BpPage;
import alchemystar.freedom.store.page.Page;
import alchemystar.freedom.store.page.PageLoader;
import alchemystar.freedom.store.page.PagePool;
import alchemystar.transaction.log.Log;

/**
 * FStore
 *
 * @Author lizhuyang
 */
public class FStore {

    // 文件路径
    private String filePath;
    // 文件channel
    private FileChannel fileChannel;
    // 当前filePosition
    private long currentFilePosition;

    public FStore(String filePath) {
        this.filePath = filePath;
        currentFilePosition = 0;
        open();
    }

    public void open() {
        fileChannel = FileUtils.open(filePath);
    }

    public Page readPageFromFile(int pageIndex) {
        return readPageFromFile(pageIndex, false);
    }

    public Page readPageFromFile(int pageIndex, boolean isIndex) {
        int readPos = pageIndex * SystemConfig.DEFAULT_PAGE_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(SystemConfig.DEFAULT_PAGE_SIZE);
        try {
            FileUtils.readFully(fileChannel, buffer, readPos);
        } catch (EOFException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // byteBuffer 转 buffer
        byte[] b = new byte[SystemConfig.DEFAULT_PAGE_SIZE];
        // position跳回原始位置
        buffer.flip();
        buffer.get(b);
        if (!isIndex) {
            // 从池中拿取空页
            Page page = PagePool.getIntance().getFreePage();
            // 初始化page
            page.read(b);
            return page;
        } else {
            BpPage bpPage = new BpPage(SystemConfig.DEFAULT_PAGE_SIZE);
            bpPage.read(b);
            return bpPage;
        }
    }

    public PageLoader readPageLoaderFromFile(int pageIndex) {
        Page page = readPageFromFile(pageIndex);
        PageLoader loader = new PageLoader(page);
        // 装载byte
        loader.load();
        return loader;
    }

    public void writePageToFile(Page page, int pageIndex) {
        try {
            int writePos = pageIndex * SystemConfig.DEFAULT_PAGE_SIZE;
            ByteBuffer byteBuffer = ByteBuffer.wrap(page.getBuffer());
            FileUtils.writeFully(fileChannel, byteBuffer, writePos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void append(Log log) {
        
    }

    public void close() {
        FileUtils.closeFile(fileChannel);
    }
}
