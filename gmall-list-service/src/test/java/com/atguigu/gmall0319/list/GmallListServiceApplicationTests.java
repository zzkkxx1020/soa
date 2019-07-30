package com.atguigu.gmall0319.list;

import com.atguigu.gmall0319.bean.SkuLsParams;
import com.atguigu.gmall0319.bean.SkuLsResult;
import com.atguigu.gmall0319.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	// 操作es的api类
	@Autowired
	private JestClient jestClient;

	@Autowired
	private ListService listService;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testES() throws IOException {
		// 编写dsl语句
		String query="{\n" +
				"  \"query\": {\n" +
				"    \"match\": {\n" +
				"      \"actorList.name\": \"张译\"\n" +
				"    }\n" +
				"  }\n" +
				"}";
		// 准备查询
		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();
		// 取得执行结果
		SearchResult result = jestClient.execute(search);
		// 根据执行结果取得Hits
		List<SearchResult.Hit<HashMap, Void>> hits = result.getHits(HashMap.class);
		// 循环取得数据
		for (SearchResult.Hit<HashMap, Void> hit : hits) {
			// _source 的map集合
			HashMap map = hit.source;
			System.out.println(map.get("name"));
		}
	}


//	@Test
//	public void  testEsDsl(){
//		SkuLsParams skuLsParams = new SkuLsParams();
//		skuLsParams.setKeyword("小米");
//		skuLsParams.setCatalog3Id("61");
//		skuLsParams.setPageNo(1);
//		skuLsParams.setPageSize(5);
//		skuLsParams.setValueId(new String[]{"83"});
//		SkuLsResult search = listService.search(skuLsParams);
//		System.out.println(search.toString());
//	}

}
