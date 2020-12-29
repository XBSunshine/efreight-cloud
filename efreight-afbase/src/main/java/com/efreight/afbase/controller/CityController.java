package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.City;
import com.efreight.afbase.entity.view.AirportCitySearch;
import com.efreight.afbase.service.CityService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@RestController
@AllArgsConstructor
@RequestMapping("/city")
public class CityController {
	private final CityService service;
	
	/**
	 * 分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public MessageInfo getListPage(Page page,City bean) {
		return MessageInfo.ok(service.getListPage(page,bean));
	}

	@PostMapping("/doSave")
	public MessageInfo saveCity(@RequestBody City city) {
		try {
			int result = service.saveCity(city);
			return MessageInfo.ok(result);
		} catch (Exception e) {
			return MessageInfo.failed(e.getMessage());
		}
	}

	@PostMapping("/doUpdate")
	public MessageInfo editCity(@RequestBody City city) {
		try {
			int result = service.editCity(city);
			return MessageInfo.ok(result);
		} catch (Exception e) {
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 删除
	 *
	 * @param cityId
	 * @return
	 */
	@DeleteMapping("/{cityId}")
	public MessageInfo delete(@PathVariable("cityId") String cityId) {
		try {
			service.removeCityById(cityId);
			return MessageInfo.ok();
		} catch (Exception e) {
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * Lc城市搜索
	 * @param key
	 * @return
	 */
	@GetMapping("searchCity/{key}")
	public MessageInfo searchCity(@PathVariable("key") String key){
		List<AirportCitySearch> searchResults = service.searchCity(key);
		return MessageInfo.ok(searchResults);
	}

	/*	*//**
	 * 数据导入
	 * @param file
	 * @return
	 * @throws IOException 
	 *//*
	@RequestMapping(value = "/importData", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> importUserData(MultipartFile file) throws IOException {
		Map<String, Object> map=new HashMap<String, Object>();
		InputStream input = null;
		try {
			input = file.getInputStream();
			String fileAllName = file.getOriginalFilename();
			Workbook wb  = null;
			//根据文件格式(2003或者2007)来初始化
			if(fileAllName.endsWith("xlsx")){
				wb = new XSSFWorkbook(input);
			}else if(fileAllName.endsWith("xls")){
				wb = new HSSFWorkbook(input);
			}else{
				map.put("success", "templateERROR");
				map.put("result", "");
				return map;
			}
			Sheet sheet = wb.getSheetAt(1);		//获得第二个表单
	        int minRowIx = sheet.getFirstRowNum()+1;  
	        int maxRowIx = sheet.getLastRowNum(); 
	        List<City> list1 = new ArrayList<City>();
	        List<City> list2 = new ArrayList<City>();
	        List<City> list3 = new ArrayList<City>();
	        DecimalFormat df = new DecimalFormat("#");
	        for (int rowIx = minRowIx; rowIx <= maxRowIx; rowIx++) { 
	        	City bean=new City();
	        	Row row = sheet.getRow(rowIx);
	        	//城市代码
	        	String cityCode = "";
	    		try {
	    			if(row.getCell(0).getCellType()!= CellType.BLANK){
						if (row.getCell(0).getCellType()==CellType.STRING) {
							cityCode=row.getCell(0).getStringCellValue();
						} else if(row.getCell(0).getCellType()==CellType.NUMERIC){
							cityCode=df.format(row.getCell(0).getNumericCellValue());
						}
					}
	    			cityCode = "null".equals(cityCode) ? "":cityCode;
				} catch (Exception e) {
				}
	    		bean.setCityCode(cityCode);
	    		//国家代码
	    		String nationCode = "";
	    		try {
	    			if(row.getCell(1).getCellType()!=CellType.BLANK){
						if (row.getCell(1).getCellType()==CellType.STRING) {
							nationCode=row.getCell(1).getStringCellValue();
						} else if(row.getCell(1).getCellType()==CellType.NUMERIC){
							nationCode=df.format(row.getCell(1).getNumericCellValue());
						}
					}
	    			nationCode = "null".equals(nationCode) ? "":nationCode;
				} catch (Exception e) {
				}
	    		bean.setNationCode(nationCode);
	    		//城市名称
	    		String cityName = "";
	    		try {
	    			if(row.getCell(2).getCellType()!=CellType.BLANK){
						if (row.getCell(2).getCellType()==CellType.STRING) {
							cityName=row.getCell(2).getStringCellValue();
						} else if(row.getCell(2).getCellType()==CellType.NUMERIC){
							cityName=df.format(row.getCell(2).getNumericCellValue());
						}
					}
	    			cityName = "null".equals(cityName) ? "":cityName;
				} catch (Exception e) {
				}
	    		bean.setCityName(cityName);
	    		//城市英文名
	    		String cityEname = "";
	    		try {
	    			if(row.getCell(3).getCellType()!=CellType.BLANK){
						if (row.getCell(3).getCellType()==CellType.STRING) {
							cityEname=row.getCell(3).getStringCellValue();
						} else if(row.getCell(3).getCellType()==CellType.NUMERIC){
							cityEname=df.format(row.getCell(3).getNumericCellValue());
						}
					}
	    			cityEname = "null".equals(cityEname) ? "":cityEname;
	    		} catch (Exception e) {
	    		}
	    		bean.setCityEname(cityEname);
	    		//城市电话区号
	    		String cityTelcode = "";
	    		try {
	    			if(row.getCell(4).getCellType()!=CellType.BLANK){
	    				if (row.getCell(4).getCellType()==CellType.STRING) {
	    					cityTelcode=row.getCell(4).getStringCellValue();
	    				} else if(row.getCell(4).getCellType()==CellType.NUMERIC){
	    					cityTelcode=df.format(row.getCell(4).getNumericCellValue());
	    				}
	    			}
	    			cityTelcode = "null".equals(cityTelcode) ? "":cityTelcode;
	    		} catch (Exception e) {
	    		}
	    		bean.setCityTelcode(cityTelcode);
	    		
	    		bean.setCityStatus(true);
	    		if (rowIx<2000) {
	    			list1.add(bean);
				} else if (rowIx<3800){
					list2.add(bean);
				}else{
					list3.add(bean);
				}
	        }
	        
	        if (list1.size()>0) {
	        	new Thread(new Runnable() {
	  	          @Override
	  	          public void run() {
	  	        	service.importData(list1);
	  	          }
	  	      }).start();	
			} else {
				map.put("success", "templateEmpty");
				map.put("result", "");
			}
	        if (list2.size()>0) {
	        	new Thread(new Runnable() {
	  	          @Override
	  	          public void run() {
	  	        	service.importData(list2);
	  	          }
	  	      }).start();
	        		
			}
	        if (list3.size()>0) {
	        	new Thread(new Runnable() {
	        		@Override
	        		public void run() {
	        			service.importData(list3);
	        		}
	        	}).start();
	        	
	        }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//关闭
	        input.close();
		}
		return map;
	}*/
}

