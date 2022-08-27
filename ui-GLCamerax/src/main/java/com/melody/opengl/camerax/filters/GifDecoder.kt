package com.melody.opengl.camerax.filters

import android.graphics.Bitmap
import java.io.InputStream
import java.lang.Exception
import java.util.*

//Handler for read & extract Bitmap from *.gif
open class GifDecoder {
    // to store *.gif data, Bitmap & delay
    inner class GifFrame(// to access image & delay w/o interfaces
        var image: Bitmap, var delay: Int
    )

    private var status = 0
    private var `in`: InputStream? = null

    // to get its Width / Height
    // full image width
    private var width = 0
    // full image height
    private var heigh = 0
    // global color table used
    private var gctFlag = false
    // size of global color table
    private var gctSize = 0
    private var loopCount = 1 // iterations; 0 = repeat forever
    // global color table
    private var gct : IntArray? = null
    // local color table
    private var lct : IntArray? = null
    // active color table
    private var act : IntArray? = null
    // background color index
    private var bgIndex = 0
    // background color
    private var bgColor = 0
    // previous bg color
    private var lastBgColor = 0
    // pixel aspect ratio
    private var pixelAspect = 0
    // local color table flag
    private var lctFlag = false
    // interlace flag
    private var interlace = false
    // local color table size
    private var lctSize = 0
    private var ix = 0
    private var iy = 0
    private var iw = 0
    // current image rectangle
    private var ih = 0
    private var lrx = 0
    private var lry = 0
    private var lrw = 0
    private var lrh = 0
    // current frame
    private var image : Bitmap? = null
    // previous frame
    private var lastImage : Bitmap? = null
    private var frameindex = 0

    fun getFrameIndex(): Int {
        return frameindex
    }

    fun setFrameIndex(frameIndex: Int) {
        this.frameindex = frameIndex
        if (frameindex > (frames?.size?:0) - 1) {
            this.frameindex = 0
        }
    }

    private var block = ByteArray(256) // current data block
    private var blockSize = 0 // block size

    // last graphic control extension info
    private var dispose = 0

    // 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
    private var lastDispose = 0
    private var transparency = false // use transparent color
    private var delay = 0 // delay in milliseconds
    private var transIndex // transparent color index
            = 0

    // max decoder pixel stack size
    // LZW decoder working arrays
    private var prefix: ShortArray? = null
    private var suffix: ByteArray? = null
    private var pixelStack: ByteArray? = null
    private var pixels: ByteArray? = null
    // frames read from current file
    private var frames : Vector<GifFrame>? = null
    var frameCount = 0
        private set

    /**
     * Gets display duration for specified frame.
     *
     * @param n
     * int index of frame
     * @return delay in milliseconds
     */
    fun getDelay(n: Int): Int {
        delay = -1
        if (n in 0 until frameCount) {
            delay = (frames!!.elementAt(n) as GifFrame).delay
        }
        return delay
    }

    fun getImage(): Bitmap? {
        return getFrame(0)
    }

    private fun setPixels() {
        val dest = IntArray(width * heigh)
        // fill in starting image contents based on last image's dispose code
        if (lastDispose > 0) {
            if (lastDispose == 3) {
                // use image before last
                val n = frameCount - 2
                lastImage = if (n > 0) {
                    getFrame(n - 1)
                } else {
                    null
                }
            }
            if (lastImage != null) {
                lastImage!!.getPixels(dest, 0, width, 0, 0, width, heigh)
                // copy pixels
                if (lastDispose == 2) {
                    // fill last image rect area with background color
                    var c = 0
                    if (!transparency) {
                        c = lastBgColor
                    }
                    for (i in 0 until lrh) {
                        val n1 = (lry + i) * width + lrx
                        val n2 = n1 + lrw
                        for (k in n1 until n2) {
                            dest[k] = c
                        }
                    }
                }
            }
        }

        // copy each source line to the appropriate place in the destination
        var pass = 1
        var inc = 8
        var iline = 0
        for (i in 0 until ih) {
            var line = i
            if (interlace) {
                if (iline >= ih) {
                    pass++
                    when (pass) {
                        2 -> iline = 4
                        3 -> {
                            iline = 2
                            inc = 4
                        }
                        4 -> {
                            iline = 1
                            inc = 2
                        }
                    }
                }
                line = iline
                iline += inc
            }
            line += iy
            if (line < heigh) {
                val k = line * width
                var dx = k + ix // start of line in dest
                var dlim = dx + iw // end of dest line
                if (k + width < dlim) {
                    dlim = k + width // past dest edge
                }
                var sx = i * iw // start of line in source
                while (dx < dlim) {
                    // map color and insert in destination
                    val index = pixels!![sx++].toInt() and 0xff
                    val c = act!![index]
                    if (c != 0) {
                        dest[dx] = c
                    }
                    dx++
                }
            }
        }
        image = Bitmap.createBitmap(dest, width, heigh, Bitmap.Config.ARGB_4444)
    }

    fun getFrame(n: Int): Bitmap? {
        var im: Bitmap? = null
        if (n >= 0 && n < frameCount) {
            im = (frames!!.elementAt(n) as GifFrame).image
        }
        return im
    }

    fun nextBitmap(): Bitmap? {
        frameindex++
        if (frameindex > frames!!.size - 1) {
            frameindex = 0
        }
        return frames?.elementAt(frameindex)?.image
    }

    fun nextDelay(): Int {
        return frames?.elementAt(frameindex)?.delay?:0
    }

    // to read & parse all *.gif stream
    fun read(`is`: InputStream?): Int {
        init()
        if (`is` != null) {
            `in` = `is`
            readHeader()
            if (!err()) {
                readContents()
                if (frameCount < 0) {
                    status = STATUS_FORMAT_ERROR
                }
            }
        } else {
            status = STATUS_OPEN_ERROR
        }
        try {
            `is`!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return status
    }

    private fun decodeImageData() {
        val NullCode = -1
        val npix = iw * ih
        var available: Int
        val clear: Int
        var code_mask: Int
        var code_size: Int
        val end_of_information: Int
        var in_code: Int
        var old_code: Int
        var bits: Int
        var code: Int
        var count: Int
        var i: Int
        var datum: Int
        val data_size: Int
        var first: Int
        var top: Int
        var bi: Int
        var pi: Int
        if (pixels == null || pixels!!.size < npix) {
            pixels = ByteArray(npix) // allocate new pixel array
        }
        if (prefix == null) {
            prefix = ShortArray(MaxStackSize)
        }
        if (suffix == null) {
            suffix = ByteArray(MaxStackSize)
        }
        if (pixelStack == null) {
            pixelStack = ByteArray(MaxStackSize + 1)
        }
        // Initialize GIF data stream decoder.
        data_size = read()
        clear = 1 shl data_size
        end_of_information = clear + 1
        available = clear + 2
        old_code = NullCode
        code_size = data_size + 1
        code_mask = (1 shl code_size) - 1
        code = 0
        while (code < clear) {
            prefix!![code] = 0
            suffix!![code] = code.toByte()
            code++
        }

        // Decode GIF pixel stream.
        bi = 0
        pi = bi
        top = pi
        first = top
        count = first
        bits = count
        datum = bits
        i = 0
        while (i < npix) {
            if (top == 0) {
                if (bits < code_size) {
                    // Load bytes until there are enough bits for a code.
                    if (count == 0) {
                        // Read a new data block.
                        count = readBlock()
                        if (count <= 0) {
                            break
                        }
                        bi = 0
                    }
                    datum += block[bi].toInt() and 0xff shl bits
                    bits += 8
                    bi++
                    count--
                    continue
                }
                // Get the next code.
                code = datum and code_mask
                datum = datum shr code_size
                bits -= code_size

                // Interpret the code
                if (code > available || code == end_of_information) {
                    break
                }
                if (code == clear) {
                    // Reset decoder.
                    code_size = data_size + 1
                    code_mask = (1 shl code_size) - 1
                    available = clear + 2
                    old_code = NullCode
                    continue
                }
                if (old_code == NullCode) {
                    pixelStack!![top++] = suffix!![code]
                    old_code = code
                    first = code
                    continue
                }
                in_code = code
                if (code == available) {
                    pixelStack!![top++] = first.toByte()
                    code = old_code
                }
                while (code > clear) {
                    pixelStack!![top++] = suffix!![code]
                    code = prefix!![code].toInt()
                }
                first = suffix!![code].toInt() and 0xff
                // Add a new string to the string table,
                if (available >= MaxStackSize) {
                    break
                }
                pixelStack!![top++] = first.toByte()
                prefix!![available] = old_code.toShort()
                suffix!![available] = first.toByte()
                available++
                if (available and code_mask == 0
                    && available < MaxStackSize
                ) {
                    code_size++
                    code_mask += available
                }
                old_code = in_code
            }

            // Pop a pixel off the pixel stack.
            top--
            pixels!![pi++] = pixelStack!![top]
            i++
        }
        i = pi
        while (i < npix) {
            pixels!![i] = 0 // clear missing pixels
            i++
        }
    }

    private fun err(): Boolean {
        return status != STATUS_OK
    }

    // to initia variable
    fun init() {
        status = STATUS_OK
        frameCount = 0
        frames = Vector()
        gct = null
        lct = null
    }

    private fun read(): Int {
        var curByte = 0
        try {
            curByte = `in`!!.read()
        } catch (e: Exception) {
            status = STATUS_FORMAT_ERROR
        }
        return curByte
    }

    private fun readBlock(): Int {
        blockSize = read()
        var n = 0
        if (blockSize > 0) {
            try {
                var count = 0
                while (n < blockSize) {
                    count = `in`!!.read(block, n, blockSize - n)
                    if (count == -1) {
                        break
                    }
                    n += count
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (n < blockSize) {
                status = STATUS_FORMAT_ERROR
            }
        }
        return n
    }

    // Global Color Table
    private fun readColorTable(ncolors: Int): IntArray? {
        val nbytes = 3 * ncolors
        var tab: IntArray? = null
        val c = ByteArray(nbytes)
        var n = 0
        try {
            n = `in`!!.read(c)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (n < nbytes) {
            status = STATUS_FORMAT_ERROR
        } else {
            tab = IntArray(256) // max size to avoid bounds checks
            var i = 0
            var j = 0
            while (i < ncolors) {
                val r = c[j++].toInt() and 0xff
                val g = c[j++].toInt() and 0xff
                val b = c[j++].toInt() and 0xff
                tab[i++] = -0x1000000 or (r shl 16) or (g shl 8) or b
            }
        }
        return tab
    }

    // Image Descriptor
    private fun readContents() {
        // read GIF file content blocks
        var done = false
        while (!(done || err())) {
            var code = read()
            when (code) {
                0x2C -> readImage()
                0x21 -> {
                    code = read()
                    when (code) {
                        0xf9 -> readGraphicControlExt()
                        0xff -> {
                            readBlock()
                            var app = ""
                            var i = 0
                            while (i < 11) {
                                app += block[i].toChar()
                                i++
                            }
                            if (app == "NETSCAPE2.0") {
                                readNetscapeExt()
                            } else {
                                skip() // don't care
                            }
                        }
                        else -> skip()
                    }
                }
                0x3b -> done = true
                0x00 -> {}
                else -> status = STATUS_FORMAT_ERROR
            }
        }
    }

    private fun readGraphicControlExt() {
        read() // block size
        val packed = read() // packed fields
        dispose = packed and 0x1c shr 2 // disposal method
        if (dispose == 0) {
            dispose = 1 // elect to keep old image if discretionary
        }
        transparency = packed and 1 != 0
        delay = readShort() * 10 // delay in milliseconds
        transIndex = read() // transparent color index
        read() // block terminator
    }

    // to get Stream - Head
    private fun readHeader() {
        var id = ""
        for (i in 0..5) {
            id += read().toChar()
        }
        if (!id.startsWith("GIF")) {
            status = STATUS_FORMAT_ERROR
            return
        }
        readLSD()
        if (gctFlag && !err()) {
            gct = readColorTable(gctSize)
            bgColor = gct!![bgIndex]
        }
    }

    private fun readImage() {
        // offset of X
        ix = readShort() // (sub)image position & size
        // offset of Y
        iy = readShort()
        // width of bitmap
        iw = readShort()
        // height of bitmap
        ih = readShort()

        // Local Color Table Flag
        val packed = read()
        lctFlag = packed and 0x80 != 0 // 1 - local color table flag

        // Interlace Flag, to array with interwoven if ENABLE, with order
        // otherwise
        interlace = packed and 0x40 != 0 // 2 - interlace flag
        // 3 - sort flag
        // 4-5 - reserved
        lctSize = 2 shl (packed and 7) // 6-8 - local color table size
        if (lctFlag) {
            lct = readColorTable(lctSize) // read table
            act = lct // make local table active
        } else {
            act = gct // make global table active
            if (bgIndex == transIndex) {
                bgColor = 0
            }
        }
        var save = 0
        if (transparency) {
            save = act!![transIndex]
            act!![transIndex] = 0 // set transparent color if specified
        }
        if (act == null) {
            status = STATUS_FORMAT_ERROR // no color table defined
        }
        if (err()) {
            return
        }
        decodeImageData() // decode pixel data
        skip()
        if (err()) {
            return
        }
        frameCount++
        // create new image to receive frame data
        image = Bitmap.createBitmap(width, heigh, Bitmap.Config.ARGB_8888)
        // createImage(width, height);
        setPixels() // transfer pixel data to image
        image?.apply {
            frames?.addElement(GifFrame(this, delay)) // add image to frame
        }
        // list
        if (transparency) {
            act!![transIndex] = save
        }
        resetFrame()
    }

    // Logical Screen Descriptor
    private fun readLSD() {
        // logical screen size
        width = readShort()
        heigh = readShort()
        // packed fields
        val packed = read()
        gctFlag = packed and 0x80 != 0 // 1 : global color table flag
        // 2-4 : color resolution
        // 5 : gct sort flag
        gctSize = 2 shl (packed and 7) // 6-8 : gct size
        bgIndex = read() // background color index
        pixelAspect = read() // pixel aspect ratio
    }

    private fun readNetscapeExt() {
        do {
            readBlock()
            if (block[0].toInt() == 1) {
                // loop count sub-block
                val b1 = block[1].toInt() and 0xff
                val b2 = block[2].toInt() and 0xff
                loopCount = b2 shl 8 or b1
            }
        } while (blockSize > 0 && !err())
    }

    // read 8 bit data
    private fun readShort(): Int {
        // read 16-bit value, LSB first
        return read() or (read() shl 8)
    }

    private fun resetFrame() {
        lastDispose = dispose
        lrx = ix
        lry = iy
        lrw = iw
        lrh = ih
        lastImage = image
        lastBgColor = bgColor
        dispose = 0
        transparency = false
        delay = 0
        lct = null
    }

    /**
     * Skips variable length blocks up to and including next zero length block.
     */
    private fun skip() {
        do {
            readBlock()
        } while (blockSize > 0 && !err())
    }

    companion object {
        // to define some error type
        const val STATUS_OK = 0
        const val STATUS_FORMAT_ERROR = 1
        const val STATUS_OPEN_ERROR = 2
        private const val MaxStackSize = 4096
    }
}