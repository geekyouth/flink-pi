package cn.java666.FlinkPi.source;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * @author Geek
 * @date 2020-03-07 18:44:07
 * 
 * https://www.kugeci.com/ 采集歌词
 * 
 */
public class KugeciHttp {
	static String path = "/tmp/lrc.txt";
	static int page = 1;
	
	public static void main(String[] args) {
		// 手动存取断点记录，增量爬取，先要保证文件存在
		List<String> lines = FileUtil.readUtf8Lines(path);
		int size = lines.size();
		
		// 倒序检索
		for (int i = size - 1; i >= 0; i--) {
			String s = lines.get(i);
			if (StrUtil.isNotBlank(s) && s.contains("page=")) {
				String between = StrUtil.subBetween(s, "page=", "==");
				if (StrUtil.isNotBlank(between)) {
					page = Integer.parseInt(between.trim());
					String line = "\n=================== 获取到最近一次中断的 page [" + page + "]，将会增量爬取 =====================\n";
					System.out.println(line);
					FileUtil.appendUtf8String(line, path);
					break;
				}
			}
			
			if (i == 0) {
				String line = "\n======================== 未检测到 page 记录，将会从头开始爬取 =======================\n";
				System.out.println(line);
				FileUtil.appendUtf8String(line, path);
			}
		}
		
		for (int i = page; i <= 20000; i++) {
			list(i);
		}
	}
	
	public static void list(int p) {
		try {
			String page_line = "\n================================== page=" + p + " ==========================================\n";
			System.out.println(page_line);
			FileUtil.appendUtf8String(page_line, path);
			
			String s = HttpUtil.get("https://www.kugeci.com/new?page=" + p);
			Document doc = Jsoup.parse(s);
			// 列表页
			Elements elements = doc.select("#app > main > div > div > div.col-md-8 > table > tbody > tr");
			if (elements == null || elements.size() == 0) {
				String end = "\n********************* 爬取完毕，收工睡觉 *********************\n";
				System.err.println(end);
				FileUtil.appendUtf8String(end, path);
				return;
			}
			
			// 每一行条目
			for (Element element : elements) {
				String href = element.select("> td:nth-child(2) > a").attr("href");
				String text = element.text();
				
				String item_line = "\n\n" + text + "\t" + href + "\n\n";
				System.out.println(item_line);
				FileUtil.appendUtf8String(item_line, path);
				
				String id = StrUtil.subAfter(href, "https://www.kugeci.com/song/", true);
				fuckHref(id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void fuckHref(String id) {
		try {
			String lrc = HttpUtil.get("https://www.kugeci.com/download/lrc/" + id);
			// lrc = lrc.replaceAll("\\[.+\\]", "").replace("\r\n", "");
			System.out.println(lrc);
			FileUtil.appendUtf8String(lrc, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
