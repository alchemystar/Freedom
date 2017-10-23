package alchemystar.freedom.store.fs;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * FileUtils
 */
public class FileUtils {

    public static boolean exists(String fileName) {
        return new File(fileName).exists();
    }

    public static FileChannel open(String fileName) {
        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            return file.getChannel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeFile(FileChannel channel) {
        try {
            channel.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void readFully(FileChannel channel, ByteBuffer dst, long position) throws IOException {
        if (channel.position() != position) {
            channel.position(position);
        }
        do {
            int r = channel.read(dst);
            if (r < 0) {
                throw new EOFException();
            }
        } while (dst.remaining() > 0);
    }

    public static void writeFully(FileChannel channel, ByteBuffer src, long position) throws IOException {
        if (channel.position() != position) {
            channel.position(position);
        }
        do {
            channel.write(src);
        } while (src.remaining() > 0);
    }

    public static void readFully(FileChannel channel, ByteBuffer dst) throws IOException {
        do {
            int r = channel.read(dst);
            if (r < 0) {
                throw new EOFException();
            }
        } while (dst.remaining() > 0);
    }

    public static void writeFully(FileChannel channel, ByteBuffer src) throws IOException {
        do {
            // 追加数据
            channel.position(channel.size());
            channel.write(src);
        } while (src.remaining() > 0);
    }
}
