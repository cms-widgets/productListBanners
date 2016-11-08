/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2016. All rights reserved.
 */

package com.huotu.hotcms.widget.productListBanners;

import com.huotu.hotcms.service.common.ContentType;
import com.huotu.hotcms.service.entity.Category;
import com.huotu.hotcms.service.entity.Gallery;
import com.huotu.hotcms.service.entity.GalleryItem;
import com.huotu.hotcms.service.entity.MallProductCategory;
import com.huotu.hotcms.service.exception.PageNotFoundException;
import com.huotu.hotcms.service.model.MallProductCategoryModel;
import com.huotu.hotcms.service.repository.CategoryRepository;
import com.huotu.hotcms.service.repository.GalleryItemRepository;
import com.huotu.hotcms.service.repository.MallProductCategoryRepository;
import com.huotu.hotcms.service.service.CategoryService;
import com.huotu.hotcms.service.service.ContentService;
import com.huotu.hotcms.service.service.GalleryItemService;
import com.huotu.hotcms.service.service.GalleryService;
import com.huotu.hotcms.widget.*;
import com.huotu.hotcms.widget.entity.PageInfo;
import com.huotu.hotcms.widget.service.PageService;
import me.jiangcai.lib.resource.service.ResourceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;


/**
 * @author CJ
 */
public class WidgetInfo implements Widget, PreProcessWidget{
    public static final String MALL_PRODUCT_SERIAL = "mallProductSerial";
    private static final Log log = LogFactory.getLog(WidgetInfo.class);
    private static final String DATA_LIST = "dataList";
    private static final String PRODUCT_CATEGORY_MODEL = "productCategoryModel";

    @Override
    public String groupId() {
        return "com.huotu.hotcms.widget.productListBanners";
    }

    @Override
    public String widgetId() {
        return "productListBanners";
    }

    @Override
    public String name(Locale locale) {
        if (locale.equals(Locale.CHINA)) {
            return "产品条幅列表";
        }
        return "productListBanners";
    }

    @Override
    public String description(Locale locale) {
        if (locale.equals(Locale.CHINA)) {
            return "这是一个产品条幅列表，你可以对组件进行自定义修改。";
        }
        return "This is a productListBanners,  you can make custom change the component.";
    }

    @Override
    public String dependVersion() {
        return "1.0";
    }

    @Override
    public WidgetStyle[] styles() {
        return new WidgetStyle[]{new DefaultWidgetStyle(), new BrandListWidgetStyle(), new RecommendWidgetStyle()};
    }

    @Override
    public Resource widgetDependencyContent(MediaType mediaType){
        if (mediaType.equals(Widget.Javascript))
            return new ClassPathResource("js/widgetInfo.js", getClass().getClassLoader());
        return null;
    }

    @Override
    public Map<String, Resource> publicResources() {
        Map<String, Resource> map = new HashMap<>();
        map.put("thumbnail/defaultStyleThumbnail.png",new ClassPathResource("thumbnail/defaultStyleThumbnail.png"
                ,getClass().getClassLoader()));
        map.put("thumbnail/productListBannerBrand.png",new ClassPathResource("thumbnail/productListBannerBrand.png"
                ,getClass().getClassLoader()));
        map.put("thumbnail/recommend.png", new ClassPathResource("thumbnail/recommend.png"
                , getClass().getClassLoader()));
        return map;
    }

    @Override
    public void valid(String styleId, ComponentProperties componentProperties) throws IllegalArgumentException {
        WidgetStyle style = WidgetStyle.styleByID(this,styleId);
        //加入控件独有的属性验证
        String productSerial = (String) componentProperties.get(MALL_PRODUCT_SERIAL);
        if (productSerial == null || productSerial.equals("")) {
            throw new IllegalArgumentException("产品数据源不能为空");
        }
    }

    @Override
    public Class springConfigClass() {
        return null;
    }


    @Override
    public ComponentProperties defaultProperties(ResourceService resourceService) throws IOException {
        ComponentProperties properties = new ComponentProperties();
        //查找商城产品数据源
        MallProductCategoryRepository mallProductCategoryRepository = getCMSServiceFromCMSContext(MallProductCategoryRepository.class);
        List<MallProductCategory> mallProductCategoryList = mallProductCategoryRepository.findBySite(CMSContext
                .RequestContext().getSite());
        if (mallProductCategoryList.isEmpty()) {
            MallProductCategory mallProductCategory = initMallProductCategory(null);
            initMallProductCategory(mallProductCategory);
            properties.put(MALL_PRODUCT_SERIAL, mallProductCategory.getSerial());
        } else {
            properties.put(MALL_PRODUCT_SERIAL, mallProductCategoryList.get(0).getSerial());
        }
        return properties;
    }

    @Override
    public void prepareContext(WidgetStyle style, ComponentProperties properties, Map<String, Object> variables
            , Map<String, String> parameters) {
        String mallProductSerial = (String) variables.get(MALL_PRODUCT_SERIAL);
        MallProductCategoryRepository mallProductCategoryRepository = getCMSServiceFromCMSContext(MallProductCategoryRepository.class);
        List<MallProductCategory> mallProductCategorys = mallProductCategoryRepository
                .findBySiteAndParent_Serial(CMSContext.RequestContext().getSite(), mallProductSerial);
        GalleryItemRepository galleryItemRepository = getCMSServiceFromCMSContext(GalleryItemRepository.class);
        List<MallProductCategoryModel> list = new ArrayList<>();
        for (MallProductCategory mallProductCategory : mallProductCategorys) {
            setContentURI(variables, mallProductCategory);
            MallProductCategoryModel mallProductCategoryModel = mallProductCategory.toMallProductCategoryModel();
            mallProductCategoryModel.setGalleryItems(galleryItemRepository.findByGallery(mallProductCategory.getGallery()));
            list.add(mallProductCategoryModel);
        }
        variables.put(DATA_LIST,list);
        MallProductCategory mallProductCategory = mallProductCategoryRepository.findBySerial(mallProductSerial);
        setContentURI(variables, mallProductCategory);
        MallProductCategoryModel mallProductCategoryModel = mallProductCategory.toMallProductCategoryModel();
        mallProductCategoryModel.setGalleryItems(galleryItemRepository.findByGallery(mallProductCategory.getGallery()));
        variables.put(PRODUCT_CATEGORY_MODEL, mallProductCategoryModel);
    }


    public MallProductCategory initMallProductCategory(MallProductCategory parent) {
        CategoryService categoryService = CMSContext.RequestContext().getWebApplicationContext()
                .getBean(CategoryService.class);
        MallProductCategoryRepository mallProductCategoryRepository = getCMSServiceFromCMSContext(MallProductCategoryRepository.class);
        MallProductCategory mallProductCategory = new MallProductCategory();
        mallProductCategory.setGoodTitle("");
        mallProductCategory.setSite(CMSContext.RequestContext().getSite());
        mallProductCategory.setName("商城产品数据源");
        mallProductCategory.setCreateTime(LocalDateTime.now());
        mallProductCategory.setContentType(ContentType.MallProduct);
        mallProductCategory.setParent(parent);
        categoryService.init(mallProductCategory);
        Gallery gallery = initGallery(initCategory());
        mallProductCategory.setGallery(gallery);
        initGalleryItem(gallery, getCMSServiceFromCMSContext(ResourceService.class));
        mallProductCategoryRepository.save(mallProductCategory);
        return mallProductCategory;
    }

    private void setContentURI(Map<String, Object> variables, MallProductCategory mallProductCategory) {
        try {
            PageInfo contentPage = getCMSServiceFromCMSContext(PageService.class)
                    .getClosestContentPage(mallProductCategory, (String) variables.get("uri"));
            mallProductCategory.setContentURI(contentPage.getPagePath());
        } catch (PageNotFoundException | NullPointerException e) {
            log.warn("...", e);
            mallProductCategory.setContentURI((String) variables.get("uri"));
        }
    }

    /**
     * 初始化数据源
     *
     * @return
     */
    public Category initCategory() {
        CategoryService categoryService = getCMSServiceFromCMSContext(CategoryService.class);
        CategoryRepository categoryRepository = getCMSServiceFromCMSContext(CategoryRepository.class);
        Category category = new Category();
        category.setContentType(ContentType.Gallery);
        category.setName("默认数据源");
        categoryService.init(category);
        category.setSite(CMSContext.RequestContext().getSite());
        //保存到数据库
        categoryRepository.save(category);
        return category;
    }

    /**
     * 初始化一个图库
     *
     * @return
     */
    public Gallery initGallery(Category category) {
        GalleryService galleryService = getCMSServiceFromCMSContext(GalleryService.class);
        ContentService contentService = getCMSServiceFromCMSContext(ContentService.class);
        Gallery gallery = new Gallery();
        gallery.setTitle("默认图库标题");
        gallery.setDescription("这是一个默认图库");
        gallery.setCategory(category);
        contentService.init(gallery);
        galleryService.saveGallery(gallery);
        return gallery;
    }

    /**
     * 初始化一个图片
     *
     * @param gallery
     * @param resourceService
     * @return
     */
    public GalleryItem initGalleryItem(Gallery gallery, ResourceService resourceService) {
        ContentService contentService = getCMSServiceFromCMSContext(ContentService.class);
        GalleryItemService galleryItemService = getCMSServiceFromCMSContext(GalleryItemService.class);
        GalleryItem galleryItem = new GalleryItem();
        galleryItem.setTitle("默认图片标题");
        galleryItem.setDescription("这是一个默认图片");
        ClassPathResource classPathResource = new ClassPathResource("thumbnail.png", getClass().getClassLoader());
        InputStream inputStream = null;
        try {
            inputStream = classPathResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String imgPath = "_resources/" + UUID.randomUUID().toString() + ".png";
        try {
            resourceService.uploadResource(imgPath, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        galleryItem.setThumbUri(imgPath);
        galleryItem.setSize("xxx");
        galleryItem.setGallery(gallery);
        contentService.init(galleryItem);
        galleryItemService.saveGalleryItem(galleryItem);
        return galleryItem;
    }

}
