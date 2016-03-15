package com.ith.eventparcerfx1;

import java.util.List;
import org.jsoup.Jsoup;

public class Establishment {

    private String name = "";
    private String desc = "";
    private String category = "";
    private String lat = "";
    private String lon = "";
    private String telephone = "";
    private String address = "";
    private List<String> images;
    private double rating;
    private String site = "";

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Establishment)) {
            return false;
        }

        Establishment other = (Establishment) o;
        return (this.name.equals(other.name) && this.address.equals(other.address));
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        address = address.replaceAll(";", ",");
        this.address = address;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        //desc = desc.replaceAll(";", ",");
        this.desc = desc;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        if (images.size() > 7) {
            this.images = images.subList(0, 7);
        } else {
            this.images = images;
        }
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
       // name = name.replaceAll(";", ",");
        this.name = name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Establishment(String address, String desc, String category, List<String> images, String lat, String lon, String name, double rating, String telephone, String site) {
        name = Jsoup.parse(name).text();
        desc = Jsoup.parse(desc).text();
        address = Jsoup.parse(address).text();

        name = name.replaceAll(";", ",");
        desc = desc.replaceAll(";", ",");
        address = address.replaceAll(";", ",");

        this.address = address;
        this.desc = desc;
        if (images.size() > 7) {
            this.images = images.subList(0, 7);
        } else {
            this.images = images;
        }
        this.category = category;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.rating = rating;
        this.telephone = telephone;
        this.site = site;
    }

    //name description website latitude longitude address image_links rating
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\""+name).append("\", \"").append(desc).append("\", \"").
                append(site).append("\", \"").append(lat).append("\", \"").append(lon).
                append("\", \"").append(address).append("\", \"");
        if (images != null ) {
            for (String image : images) {
                if (!(image.contains("http://") || image.contains("https://"))) {
                    sb.append("http://").append(image).append(",");
                } else {
                    sb.append(image).append(",");
                }
            }
        }
        if (images == null || images.size()==0)   
             sb.append(",");
        sb.substring(0, sb.length() - 2);
        StringBuilder sb1 = new StringBuilder();
        sb1.append(sb.toString().substring(0, sb.toString().length() - 1));
        int a = (int) Math.round(rating);
        if (rating == -1) {
            sb1.append("\", \"\"\n");
        } else {
            sb1.append("\", \"").append(a).append("\"\n");
        }
        return sb1.toString();
    }

    public boolean isValid() {
        return (!this.address.isEmpty())
                && (!this.name.isEmpty())
                && (!this.lat.isEmpty())
                && (!this.lon.isEmpty())
                && (!this.address.isEmpty());

    }
}
