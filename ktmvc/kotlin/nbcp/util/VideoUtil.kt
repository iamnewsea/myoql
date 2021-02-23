package nbcp.util

import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.File
import javax.imageio.ImageIO

object VideoUtil {

    /**
     * @return 返回错误消息
     */
    fun getVideoLogo(videoFile: File, targetLogoFile: File): String {
        FFmpegFrameGrabber(videoFile).use { fFmpegFrameGrabber ->
            fFmpegFrameGrabber.start();


            val ftp = fFmpegFrameGrabber.lengthInFrames

            var index = -1;

            while (index <= ftp) {
                index++;

                var frame = fFmpegFrameGrabber.grabImage()
                if (frame == null) {
                    break;
                }

                if (index == 5) {
                    ImageIO.write(Java2DFrameConverter().getBufferedImage(frame), "jpg", targetLogoFile)
                    return "";
                }
            }

            fFmpegFrameGrabber.stop()
        }

        return "";
    }
}