package com.efreight.afbase.entity.cargo.track;

import com.efreight.afbase.entity.view.ManifestVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lc
 * @date 2020/12/3 14:21
 */
@Data
public class CargoTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总量
     */
    private Integer total;

    /**
     * 使用量
     */
    private Integer used;

    /**
     * 轨迹信息
     */
    private List<CargoRoute> routeTracks;

    /**
     * 舱单信息（原始）
     */
    private List<CargoRoute> trackManifest;

    /**
     * 舱单信息（处理）
     */
    private List<ManifestVO> manifestList;
}
