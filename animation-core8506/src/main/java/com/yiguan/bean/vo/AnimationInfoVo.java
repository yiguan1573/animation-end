package com.yiguan.bean.vo;

import com.yiguan.bean.entity.AnimationInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: lw
 * @CreateTime: 2022-11-17  14:46
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimationInfoVo extends AnimationInfo {
    //播放量
    private Integer viewCounts;
    //弹幕量
    private Integer bulletChat;
    //加载的弹幕量
    private Integer loadBulletChat;
    //是否已经收藏1为已收藏0为未收藏
    private Integer status=0;
    //正片
    private List<EpisodeFileVo> episodeFileVos;
}
