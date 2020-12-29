package com.efreight.prm.service.impl;

import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.InquiryAgentDao;
import com.efreight.prm.entity.Coop;
import com.efreight.prm.entity.InquiryAgent;
import com.efreight.prm.service.InquiryAgentService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * PRM 询盘代理 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2020-05-18
 */
@Service
public class InquiryAgentServiceImpl implements InquiryAgentService {

    @Autowired
    private InquiryAgentDao dao;

    @Override
    public Map<String, Object> queryList(Integer currentPage, Integer pageSize, Map<String, Object> paramMap) {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        if (currentPage == null || currentPage == 0)
            currentPage = 1;
        if (pageSize == null || pageSize == 0)
            pageSize = 10;
        Page Page = PageHelper.startPage(currentPage, pageSize, true);
        List<InquiryAgent> persons = dao.queryList(paramMap);
        //联系人
        for (int i = 0; i < persons.size(); i++) {
            InquiryAgent bean = persons.get(i);
            String bookingContactsId = bean.getBookingContactsId();
            if (bookingContactsId != null) {
                Map<String, Object> paramContactsMap = new HashMap<>();
                paramContactsMap.put("orgId", paramMap.get("orgId"));
                paramContactsMap.put("contactsId", "(" + bookingContactsId + ")");
                List<Map<String, Object>> nameList = dao.queryContactsList(paramContactsMap);

                String bookingContactsName = "";
                for (int j = 0; j < nameList.size(); j++) {
                    if (bookingContactsName.length() == 0) {
                        bookingContactsName = nameList.get(j).get("contacts_name").toString();
                    } else {
                        bookingContactsName = bookingContactsName + ", " + nameList.get(j).get("contacts_name").toString();
                    }
                }
                bean.setBookingContactsName(bookingContactsName);
            }
            Integer inquiryOrderAmount = dao.countInquiryOrderAmount(bean.getInquiryId().toString());
            bean.setInquiryOrderAmount(inquiryOrderAmount);
        }
        long countNums = Page.getTotal();//总记录数
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
    }

    @Override
    public Map<String, Object> getInquiryAgentList(Integer currentPage, Integer pageSize, Map<String, Object> paramMap) {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        if (currentPage == null || currentPage == 0)
            currentPage = 1;
        if (pageSize == null || pageSize == 0)
            pageSize = 10;
        Page Page = PageHelper.startPage(currentPage, pageSize, true);
        List<InquiryAgent> persons = dao.getInquiryAgentList(paramMap);
        //联系人
        for (int i = 0; i < persons.size(); i++) {
            InquiryAgent bean = persons.get(i);
            String bookingContactsId = bean.getBookingContactsId();
            if (bookingContactsId != null) {
                Map<String, Object> paramContactsMap = new HashMap<>();
                paramContactsMap.put("orgId", bean.getOrgId());
                paramContactsMap.put("contactsId", "(" + bookingContactsId + ")");
                List<Map<String, Object>> nameList = dao.queryContactsList(paramContactsMap);

                String bookingContactsName = "";
                for (int j = 0; j < nameList.size(); j++) {
                    if (bookingContactsName.length() == 0) {
                        bookingContactsName = nameList.get(j).get("contacts_name").toString();
                    } else {
                        bookingContactsName = bookingContactsName + ", " + nameList.get(j).get("contacts_name").toString();
                    }
                }
                bean.setBookingContactsName(bookingContactsName);
            }
            if (SecurityUtils.getUser().getOrgId().equals(bean.getOrgId())) {
                Integer inquiryOrderAmount = dao.countInquiryOrderAmount(bean.getInquiryId().toString());
                bean.setInquiryOrderAmount(inquiryOrderAmount);
            }
        }
        long countNums = Page.getTotal();//总记录数
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
    }

    @Override
    public int doSave(InquiryAgent bean) {
        String carrierCode = "";
        if (bean.getCarrierCodes() != null) {
            for (int i = 0; i < bean.getCarrierCodes().size(); i++) {
                if (carrierCode.length() == 0) {
                    carrierCode = "" + bean.getCarrierCodes().get(i);
                } else {
                    carrierCode = carrierCode + ", " + bean.getCarrierCodes().get(i);
                }

            }
        }

        bean.setCarrierCode(carrierCode);

        String departureStation = "";
        if (bean.getDepartureStations() != null) {
            for (int i = 0; i < bean.getDepartureStations().size(); i++) {
                if (departureStation.length() == 0) {
                    departureStation = "" + bean.getDepartureStations().get(i);
                } else {
                    departureStation = departureStation + ", " + bean.getDepartureStations().get(i);
                }

            }
        }
        bean.setDepartureStation(departureStation);

        String arrivalStation = "";
        if (bean.getArrivalStations() != null) {
            for (int i = 0; i < bean.getArrivalStations().size(); i++) {
                if (arrivalStation.length() == 0) {
                    arrivalStation = "" + bean.getArrivalStations().get(i);
                } else {
                    arrivalStation = arrivalStation + ", " + bean.getArrivalStations().get(i);
                }

            }
        }
        bean.setArrivalStation(arrivalStation);

        String nationCodeArrival = "";
        if (bean.getNationCodeArrivals() != null) {
            for (int i = 0; i < bean.getNationCodeArrivals().size(); i++) {
                if (nationCodeArrival.length() == 0) {
                    nationCodeArrival = "" + bean.getNationCodeArrivals().get(i);
                } else {
                    nationCodeArrival = nationCodeArrival + ", " + bean.getNationCodeArrivals().get(i);
                }

            }
        }
        bean.setNationCodeArrival(nationCodeArrival);

        String bookingContactsId = "";
        for (int i = 0; i < bean.getOrderContacts().size(); i++) {
            if (bookingContactsId.length() == 0) {
                bookingContactsId = "" + bean.getOrderContacts().get(i);
            } else {
                bookingContactsId = bookingContactsId + "," + bean.getOrderContacts().get(i);
            }

        }
        bean.setBookingContactsId(bookingContactsId);

        dao.doSave(bean);
        return bean.getInquiryAgentId();
    }

    @Override
    public InquiryAgent queryById(Map<String, Object> paramMap) {
        InquiryAgent bean = dao.queryById(paramMap);

        if (bean.getBookingContactsId() != null && !"".equals(bean.getBookingContactsId())) {
            List<String> stringList = Arrays.asList((bean.getBookingContactsId().split(",")));
            List<Integer> intList = new ArrayList<Integer>();
            for (int i = 0; i < stringList.size(); i++) {
                intList.add(Integer.parseInt(stringList.get(i)));
            }
            bean.setOrderContacts(intList);
        }
        if (bean.getCarrierCode() != null && !"".equals(bean.getCarrierCode())) {
            bean.setCarrierCodes(Arrays.asList(bean.getCarrierCode().split(",")));
        }
        if (bean.getDepartureStation() != null && !"".equals(bean.getDepartureStation())) {
            bean.setDepartureStations(Arrays.asList(bean.getDepartureStation().split(",")));
        }
        if (bean.getArrivalStation() != null && !"".equals(bean.getArrivalStation())) {
            bean.setArrivalStations(Arrays.asList(bean.getArrivalStation().split(",")));
        }
        if (bean.getNationCodeArrival() != null && !"".equals(bean.getNationCodeArrival())) {
            bean.setNationCodeArrivals(Arrays.asList(bean.getNationCodeArrival().split(",")));
        }

        return bean;
    }

    @Override
    public void doEdit(InquiryAgent bean) {
        String carrierCode = "";
        if (bean.getCarrierCodes() != null) {
            for (int i = 0; i < bean.getCarrierCodes().size(); i++) {
                if (carrierCode.length() == 0) {
                    carrierCode = "" + bean.getCarrierCodes().get(i);
                } else {
                    carrierCode = carrierCode + "," + bean.getCarrierCodes().get(i);
                }

            }
        }

        bean.setCarrierCode(carrierCode);

        String departureStation = "";
        if (bean.getDepartureStations() != null) {
            for (int i = 0; i < bean.getDepartureStations().size(); i++) {
                if (departureStation.length() == 0) {
                    departureStation = "" + bean.getDepartureStations().get(i);
                } else {
                    departureStation = departureStation + "," + bean.getDepartureStations().get(i);
                }

            }
        }
        bean.setDepartureStation(departureStation);

        String arrivalStation = "";
        if (bean.getArrivalStations() != null) {
            for (int i = 0; i < bean.getArrivalStations().size(); i++) {
                if (arrivalStation.length() == 0) {
                    arrivalStation = "" + bean.getArrivalStations().get(i);
                } else {
                    arrivalStation = arrivalStation + "," + bean.getArrivalStations().get(i);
                }

            }
        }
        bean.setArrivalStation(arrivalStation);

        String nationCodeArrival = "";
        if (bean.getNationCodeArrivals() != null) {
            for (int i = 0; i < bean.getNationCodeArrivals().size(); i++) {
                if (nationCodeArrival.length() == 0) {
                    nationCodeArrival = "" + bean.getNationCodeArrivals().get(i);
                } else {
                    nationCodeArrival = nationCodeArrival + "," + bean.getNationCodeArrivals().get(i);
                }

            }
        }
        bean.setNationCodeArrival(nationCodeArrival);

        String bookingContactsId = "";
        for (int i = 0; i < bean.getOrderContacts().size(); i++) {
            if (bookingContactsId.length() == 0) {
                bookingContactsId = "" + bean.getOrderContacts().get(i);
            } else {
                bookingContactsId = bookingContactsId + "," + bean.getOrderContacts().get(i);
            }

        }
        bean.setBookingContactsId(bookingContactsId);
        dao.doEdit(bean);
    }

    @Override
    public Map<String, Object> selectCarrierCode(Map<String, Object> paramMap) {
        List<Map<String, Object>> list = dao.selectCarrierCode(paramMap);
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", 0);
        rerultMap.put("dataList", list);
        return rerultMap;
    }

    @Override
    public Map<String, Object> selectAirport(Map<String, Object> paramMap) {
        List<Map<String, Object>> list = dao.selectAirport(paramMap);
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", 0);
        rerultMap.put("dataList", list);
        return rerultMap;
    }

    @Override
    public Map<String, Object> selectNation(Map<String, Object> paramMap) {
        List<Map<String, Object>> list = dao.selectNation(paramMap);
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", 0);
        rerultMap.put("dataList", list);
        return rerultMap;
    }

    @Override
    public Map<String, Object> selectContacts(Map<String, Object> paramMap) {
        List<Map<String, Object>> list = dao.selectContacts(paramMap);
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", 0);
        rerultMap.put("dataList", list);
        return rerultMap;
    }

    @Override
    public List<InquiryAgent> exportExcel(InquiryAgent bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<InquiryAgent> list = dao.exportExcel(bean);
        list.stream().forEach(inquiryAgent -> {
            String bookingContactsId = inquiryAgent.getBookingContactsId();
            if (bookingContactsId != null) {
                Map<String, Object> paramContactsMap = new HashMap<>();
                paramContactsMap.put("orgId", bean.getOrgId());
                paramContactsMap.put("contactsId", "(" + bookingContactsId + ")");
                List<Map<String, Object>> nameList = dao.queryContactsList(paramContactsMap);

                String bookingContactsName = "";
                for (int j = 0; j < nameList.size(); j++) {
                    if (bookingContactsName.length() == 0) {
                        bookingContactsName = nameList.get(j).get("contacts_name").toString();
                    } else {
                        bookingContactsName = bookingContactsName + ", " + nameList.get(j).get("contacts_name").toString();
                    }
                }
                inquiryAgent.setBookingContactsName(bookingContactsName);
            }
            Integer inquiryOrderAmount = dao.countInquiryOrderAmount(inquiryAgent.getInquiryId().toString());
            inquiryAgent.setInquiryOrderAmount(inquiryOrderAmount);
        });
        return list;
    }
}
