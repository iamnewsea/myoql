package nbcp.base.utils

import nbcp.base.extend.AsString
import nbcp.base.extend.IsIn
import java.awt.Image
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO


/**
 * Created by udi on 17-4-14.
 */

object ImageUtil {

    /**
     * 按指定高度 等比例缩放图片
     * @param sourceImage
     * @param target
     * @param maxWidth 新图的宽度
     * @param maxHeight: 新图高度
     * @return 返回文件类型。
     */
    @JvmStatic
    fun zoomImageScale(
        sourceImage: InputStream,
        target: OutputStream,
        maxWidth: Int,
        maxHeight: Int
    ): nbcp.base.comm.ApiResult<String> {
        if (maxWidth < 1) {
            return nbcp.base.comm.ApiResult.error("目标宽度参数不合法");
        }

        if (maxHeight < 1) {
            return nbcp.base.comm.ApiResult.error("目标高度参数不合法");
        }

        var length = 8;
        sourceImage.mark(length);
        var ary = ByteArray(length);
        if (sourceImage.read(ary, 0, length) < length) {
            return nbcp.base.comm.ApiResult();
        }
        sourceImage.reset();

        val type = FileUtil.getFileTypeWithBom(ary).AsString("png");

        val oriImage = ImageIO.read(sourceImage)

        val originalWidth = oriImage.width
        val originalHeight = oriImage.height
        val width_scale = originalWidth * 1.0F / maxWidth     // 缩放的比例
        val height_scale = originalHeight * 1.0F / maxHeight   // 缩放的比例
        val scale = Math.max(width_scale, height_scale);

        if (scale > 1) {
            ImageIO.write(oriImage, type, target)
            return nbcp.base.comm.ApiResult.of(type);
        }

        var maxWidthValue = (originalWidth / scale).toInt()
        var maxHeightValue = (originalHeight / scale).toInt()

        var newImage = BufferedImage(maxWidthValue, maxHeightValue, BufferedImage.TYPE_INT_RGB)

        // 处理 png 背景变黑的问题
        if (type.IsIn("png", "gif")) {
            var g2d = newImage.createGraphics()
            newImage =
                g2d.deviceConfiguration.createCompatibleImage(maxWidthValue, maxHeightValue, Transparency.TRANSLUCENT)
            g2d.dispose()
        }

        var g2d = newImage.createGraphics()
        val from = oriImage.getScaledInstance(maxWidthValue, maxHeightValue, Image.SCALE_AREA_AVERAGING)
        g2d.drawImage(from, 0, 0, maxWidthValue, maxHeightValue, null)
        g2d.dispose()

        ImageIO.write(newImage, type, target)

        // 高质量压缩，其实对清晰度而言没有太多的帮助
        //            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //            tag.getGraphics().drawImage(bufferedImage, 0, 0, width, height, null);
        //
        //            FileOutputStream out = new FileOutputStream(newPath);    // 将图片写入 newPath
        //            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        //            JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(tag);
        //            jep.setQuality(1f, true);    //压缩质量, 1 是最高值
        //            encoder.encode(tag, jep);
        //            out.close();

//            val newImage = BufferedImage(width, height, bufferedImage.type)
//            val g = newImage.graphics
//            g.drawImage(bufferedImage, 0, 0, width, height, null)
//            g.dispose()
//            if (ImageIO.write(newImage, suffix, destFile) == false) {
//                return "写入新文件失败: ${destFile.FullName}"
//        }

        return nbcp.base.comm.ApiResult.error(type)
    }
}