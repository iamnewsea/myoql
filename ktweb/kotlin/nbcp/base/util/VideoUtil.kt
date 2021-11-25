package nbcp.base.util

import nbcp.comm.ApiResult
import nbcp.comm.AsInt
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.*
import javax.imageio.ImageIO
import javax.imageio.stream.ImageOutputStream
import javax.imageio.stream.MemoryCacheImageOutputStream

object VideoUtil {

    data class VideoDataInfoModel(
        var logoStream: InputStream,
        var width: Int,
        var height: Int,
        var time: Int
    )

    /**
     * @return 返回错误消息
     */
    fun getVideoInfo(videoFileStream: InputStream): ApiResult<VideoDataInfoModel> {
        FFmpegFrameGrabber(videoFileStream).use { fFmpegFrameGrabber ->
            fFmpegFrameGrabber.start();
            val ftp = fFmpegFrameGrabber.lengthInFrames
            if (ftp <= 0) {
                fFmpegFrameGrabber.stop()
                return ApiResult.error("视频没有内容")
            }

            //取第1帧做封装

            var frame = fFmpegFrameGrabber.grabImage()
            if (frame == null) {
                fFmpegFrameGrabber.stop()
                return ApiResult.error("视频没有内容")
            }


            var outputStream = ByteArrayOutputStream()
            ImageIO.write(Java2DFrameConverter().getBufferedImage(frame), "jpg", outputStream)


            var imgHeight = fFmpegFrameGrabber.imageHeight;
            var imgWidth = fFmpegFrameGrabber.imageWidth;
            var videoTime = (ftp / fFmpegFrameGrabber.frameRate / 60).AsInt();


            fFmpegFrameGrabber.stop()

            var inputStream = ByteArrayInputStream(outputStream.toByteArray());
            return ApiResult.of(VideoDataInfoModel(inputStream, imgWidth, imgHeight, videoTime))
        }
    }
}