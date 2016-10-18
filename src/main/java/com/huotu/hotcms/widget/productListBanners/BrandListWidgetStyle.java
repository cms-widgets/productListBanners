/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2016. All rights reserved.
 */

package com.huotu.hotcms.widget.productListBanners;

import com.huotu.hotcms.widget.WidgetStyle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Locale;

/**
 * @author CJ
 */
public class BrandListWidgetStyle implements WidgetStyle{

    @Override
    public String id() {
        return "productListBrandListWidgetStyle";
    }

    @Override
    public String name(Locale locale) {
        if (locale.equals(Locale.CHINA)) {
            return "品牌列表样式";
        }
        return "brand list style";
    }

    @Override
    public String description(Locale locale) {
        if (locale.equals(Locale.CHINA)) {
            return "基于bootstrap样式的风格品牌列表样式";
        }
        return "Based on the bootstrap style by productListBanners";
    }

    @Override
    public Resource thumbnail() {
        return new ClassPathResource("/thumbnail/productListBannerBrand.png", getClass().getClassLoader());
    }

    @Override
    public Resource previewTemplate() {
        return null;
    }

    @Override
    public Resource browseTemplate() {
        return new ClassPathResource("/template/brandsListBrowseTemplate.html", getClass().getClassLoader());
    }

}
