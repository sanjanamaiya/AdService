package com.ir.project.adservice.jsonFiles;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProductInfo 
{
	private SalesRank salesRank;

    private String imUrl;

    private String title;

    private String price;

    private String asin;

    private String brand;

    private String[][] categories;

    private Related related;

    @JsonIgnore
    public SalesRank getSalesRank ()
    {
        return salesRank;
    }

    @JsonIgnore
    public void setSalesRank (SalesRank salesRank)
    {
        this.salesRank = salesRank;
    }

    public String getImUrl ()
    {
        return imUrl;
    }

    public void setImUrl (String imUrl)
    {
        this.imUrl = imUrl;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getPrice ()
    {
        return price;
    }

    public void setPrice (String price)
    {
        this.price = price;
    }

    public String getAsin ()
    {
        return asin;
    }

    public void setAsin (String asin)
    {
        this.asin = asin;
    }

    public String getBrand ()
    {
        return brand;
    }

    public void setBrand (String brand)
    {
        this.brand = brand;
    }

    public String[][] getCategories ()
    {
        return categories;
    }

    public void setCategories (String[][] categories)
    {
        this.categories = categories;
    }

    public Related getRelated ()
    {
        return related;
    }

    public void setRelated (Related related)
    {
        this.related = related;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [salesRank = "+salesRank+", imUrl = "+imUrl+", title = "+title+", price = "+price+", asin = "+asin+", brand = "+brand+", categories = "+categories+", related = "+related+"]";
    }

}
