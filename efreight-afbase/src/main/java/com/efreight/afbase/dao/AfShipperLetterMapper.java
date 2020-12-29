package com.efreight.afbase.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.AfShipperLetter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * AF 订单管理 出口订单 托书信息 Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2019-10-10
 */
public interface AfShipperLetterMapper extends BaseMapper<AfShipperLetter> {

	@Select({ "<script>", "SELECT SUM(IFNULL(confirm_pieces,plan_pieces)) plan_pieces,SUM(IFNULL(confirm_weight,plan_weight)) plan_weight FROM af_order ",
			"where awb_id=#{awb_id}  and org_id=#{org_id}", 
			"</script>" })
	AfShipperLetter getSum(@Param("awb_id") Integer awb_id,@Param("org_id") Integer org_id);
	@Select({ "<script>", "SELECT * FROM af_order ",
		"where awb_id=#{awb_id}  and org_id=#{org_id}", 
	"</script>" })
	List<AfOrder> getOrderList(@Param("awb_id") Integer awb_id,@Param("org_id") Integer org_id);
	@Select({ "<script>", "SELECT * FROM af_order ",
		"where order_id=#{order_id}  and org_id=#{org_id}", 
	"</script>" })
	List<AfOrder> getOrderList2(@Param("order_id") Integer order_id,@Param("org_id") Integer org_id);
	@Select({ "<script>", "SELECT a.*,b.awb_id,b.awb_number mawbNumber,b.order_code,b.departure_station FROM af_shipper_letter a ",
		" left join af_order b on a.order_id=b.order_id",
		" where a.order_id=#{order_id}  and a.org_id=#{org_id} and a.sl_type=#{sl_type}", 
		"</script>" })
	List<AfShipperLetter> getLetterList(@Param("order_id") Integer order_id,@Param("org_id") Integer org_id,@Param("sl_type") String sl_type);
	@Select({ "<script>", "SELECT a.*,b.awb_id,b.awb_number mawbNumber,b.departure_station FROM af_shipper_letter a ",
		" left join af_order b on a.order_id=b.order_id",
		" where a.awb_id=#{awb_id}  and a.org_id=#{org_id} and a.sl_type=#{sl_type}", 
	"</script>" })
	List<AfShipperLetter> getMAWBLetterList(@Param("awb_id") Integer awb_id,@Param("org_id") Integer org_id,@Param("sl_type") String sl_type);
}
