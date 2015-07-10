//(‑●‑●)> released under the WTFPL v2 license, by Gregory Pakosz (@gpakosz)
package net.jawr.web.util.bom;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * The <code>UnicodeBOMInputStream</code> class wraps any
 * <code>InputStream</code> and detects the presence of any Unicode BOM
 * (Byte Order Mark) at its beginning, as defined by
 * <a href="http://www.faqs.org/rfcs/rfc3629.html">RFC 3629 - UTF-8, a transformation format of ISO 10646</a>
 * 
 * <p>The
 * <a href="http://www.unicode.org/unicode/faq/utf_bom.html">Unicode FAQ</a>
 * defines 5 types of BOMs:<ul>
 * <li><pre>00 00 FE FF  = UTF-32, big-endian</pre></li>
 * <li><pre>FF FE 00 00  = UTF-32, little-endian</pre></li>
 * <li><pre>FE FF        = UTF-16, big-endian</pre></li>
 * <li><pre>FF FE        = UTF-16, little-endian</pre></li>
 * <li><pre>EF BB BF     = UTF-8</pre></li>
 * </ul></p>
 * 
 * <p>Use the {@link #getBOM()} method to know whether a BOM has been detected
 * or not.
 * </p>
 * <p>Use the {@link #skipBOM()} method to remove the detected BOM from the
 * wrapped <code>InputStream</code> object.</p>
 * 
 * @author Gregory Pakosz
 * @version 1.0
 */
public class UnicodeBOMInputStream extends InputStream
{

  /**
   * Constructs a new <code>UnicodeBOMInputStream</code> that wraps the
   * specified <code>InputStream</code>.
   * 
   * @param inputStream an <code>InputStream</code>.
   * 
   * @throws NullPointerException when <code>inputStream</code> is
   * <code>null</code>.
   * @throws IOException on reading from the specified <code>InputStream</code>
   * when trying to detect the Unicode BOM.
   */
  public UnicodeBOMInputStream(final InputStream inputStream) throws  NullPointerException,
                                                                      IOException
  {
    if (inputStream == null)
      throw new NullPointerException("invalid input stream: null is not allowed");

    in = new PushbackInputStream(inputStream, 4);

    final byte  bom[] = new byte[4];
    final int   read  = in.read(bom);

    switch(read)
    {
      case 4:
        if ((bom[0] == (byte)0xFF) &&
            (bom[1] == (byte)0xFE) &&
            (bom[2] == (byte)0x00) &&
            (bom[3] == (byte)0x00))
        {
          this.bom = BOM.UTF_32_LE;
          break;
        }
        else
        if ((bom[0] == (byte)0x00) &&
            (bom[1] == (byte)0x00) &&
            (bom[2] == (byte)0xFE) &&
            (bom[3] == (byte)0xFF))
        {
          this.bom = BOM.UTF_32_BE;
          break;
        }

      case 3:
        if ((bom[0] == (byte)0xEF) &&
            (bom[1] == (byte)0xBB) &&
            (bom[2] == (byte)0xBF))
        {
          this.bom = BOM.UTF_8;
          break;
        }

      case 2:
        if ((bom[0] == (byte)0xFF) &&
            (bom[1] == (byte)0xFE))
        {
          this.bom = BOM.UTF_16_LE;
          break;
        }
        else
        if ((bom[0] == (byte)0xFE) &&
            (bom[1] == (byte)0xFF))
        {
          this.bom = BOM.UTF_16_BE;
          break;
        }

      default:
        this.bom = BOM.NONE;
        break;
    }

    if (read > 0)
      in.unread(bom, 0, read);
  }

  /**
   * Returns the <code>BOM</code> that was detected in the wrapped
   * <code>InputStream</code> object.
   *
   * @return a <code>BOM</code> value.
   */
  public final BOM getBOM()
  {
    // BOM type is immutable.
    return bom;
  }

  /**
   * Skips the <code>BOM</code> that was found in the wrapped
   * <code>InputStream</code> object.
   * 
   * @return this <code>UnicodeBOMInputStream</code>.
   * 
   * @throws IOException when trying to skip the BOM from the wrapped
   * <code>InputStream</code> object.
   */
  public final synchronized UnicodeBOMInputStream skipBOM() throws IOException
  {
    if (!skipped)
    {
      in.skip(bom.bytes.length);
      skipped = true;
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public int read() throws IOException
  {
    return in.read();
  }

  /**
   * {@inheritDoc}
   */
  public int read(final byte b[]) throws  IOException,
                                          NullPointerException
  {
    return in.read(b, 0, b.length);
  }

  /**
   * {@inheritDoc}
   */
  public int read(final byte b[],
                  final int off,
                  final int len) throws IOException,
                                        NullPointerException
  {
    return in.read(b, off, len);
  }

  /**
   * {@inheritDoc}
   */
  public long skip(final long n) throws IOException
  {
    return in.skip(n);
  }

  /**
   * {@inheritDoc}
   */
  public int available() throws IOException
  {
    return in.available();
  }

  /**
   * {@inheritDoc}
   */
  public void close() throws IOException
  {
    in.close();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void mark(final int readlimit)
  {
    in.mark(readlimit);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void reset() throws IOException
  {
    in.reset();
  }

  /**
   * {@inheritDoc}
   */
  public boolean markSupported()
  {
    return in.markSupported();
  }

  private final PushbackInputStream in;
  private final BOM                 bom;
  private       boolean             skipped = false;

} // UnicodeBOMInputStream
