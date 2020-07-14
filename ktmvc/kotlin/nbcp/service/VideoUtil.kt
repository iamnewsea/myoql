package nbcp.service

import nbcp.comm.AsInt
import nbcp.utils.CodeUtil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object VideoUtil {

    fun getVideoLogo(videoFile: File, uploadPath: String, targetLogoPath: String): String {
        var fFmpegFrameGrabber = FFmpegFrameGrabber(videoFile)
        fFmpegFrameGrabber.start();

        try {
            val ftp = fFmpegFrameGrabber.lengthInFrames

            var index = -1;

            while (index <= ftp) {
                index++;

                var frame = fFmpegFrameGrabber.grabImage()
                if (frame == null) {
                    break;
                }

                if (index == 5) {
                    ImageIO.write(FrameToBufferedImage(frame), "jpg", File(uploadPath + targetLogoPath))
                    return uploadPath;
                }
            }
        } finally {
            fFmpegFrameGrabber.stop()
            fFmpegFrameGrabber.close()
        }

        return "";
    }

    private fun FrameToBufferedImage(frame: Frame): BufferedImage { //创建BufferedImage对象
        val converter = Java2DFrameConverter()
        return converter.getBufferedImage(frame)
    }
}