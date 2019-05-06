package com.cmccpoc.util;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import android.view.View;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;

/**
 * XML模型读取类
 * @author Yao
 */
public class XmlModelReader
{
	private static final String XML_MODELS = "menu";
	private static final String XML_ITEM = "item";
	private Context mContext = null;

	public XmlModelReader(Context context)
	{
		this.mContext = context;
		
	}

	/**
	 * 寻找xml资源
	 * @param menuRes 资源文件id
	 */
	public void inflate(int menuRes)
	{
		Config.model = android.os.Build.MODEL;
		Log.i(XmlModelReader.class, "PhoneModel=" + Config.model);
		XmlResourceParser parser = null;
		try
		{
			parser = mContext.getResources().getLayout(menuRes);
			AttributeSet attrs = Xml.asAttributeSet(parser);
			parseMenu(parser, attrs);
		}
		catch (XmlPullParserException e)
		{
			throw new InflateException("Error inflating menu XML", e);
		}
		catch (IOException e)
		{
			throw new InflateException("Error inflating menu XML", e);
		}
		finally
		{
			if (parser != null)
				parser.close();
		}
	}

	/**
	 * Called internally to fill the given menu. If a sub menu is seen, it will
	 * call this recursively.
	 * @param parser XmlPullParser对象
	 * @param attrs 属性
	 * @throws XmlPullParserException XML转换异常
	 * @throws IOException IO异常
	 */
	private void parseMenu(XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException
	{
		int eventType = parser.getEventType();
		String tagName;
		boolean lookingForEndOfUnknownTag = false;
		String unknownTagName = null;

		// This loop will skip to the menu start tag
		do
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				tagName = parser.getName();
				if (tagName.equals(XML_MODELS))
				{
					// Go to next tag
					eventType = parser.next();
					break;
				}

				throw new RuntimeException("Expecting menu, got " + tagName);
			}
			eventType = parser.next();
		}
		while (eventType != XmlPullParser.END_DOCUMENT);

		boolean reachedEndOfMenu = false;
		while (!reachedEndOfMenu)
		{
			switch (eventType)
			{
				case XmlPullParser.START_TAG:
					if (lookingForEndOfUnknownTag)
					{
						break;
					}

					tagName = parser.getName();
					if (tagName.equals(XML_ITEM))
					{
						parseItem(attrs, mContext);
					}
					else
					{
						lookingForEndOfUnknownTag = true;
						unknownTagName = tagName;
					}
					break;
				case XmlPullParser.END_TAG:

					tagName = parser.getName();
					if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName))
					{
						lookingForEndOfUnknownTag = false;
						unknownTagName = null;
					}
					else if (tagName.equals(XML_MODELS))
					{
						reachedEndOfMenu = true;
					}
					break;

				case XmlPullParser.END_DOCUMENT:
					throw new RuntimeException("Unexpected end of document");
			}

			eventType = parser.next();
		}
	}

	/**
	 * 解析项
	 * @param attrs 属性
	 * @param context 上下文
	 */
	public void parseItem(AttributeSet attrs, Context context)
	{
	}
}
