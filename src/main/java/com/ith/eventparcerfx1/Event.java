package com.ith.eventparcerfx1;

import java.time.LocalDate;
import java.util.List;
import org.jsoup.Jsoup;

public class Event {

    private String name = "";
    private String desc = "";
    private String category = "Event";
    private String lat = "";
    private String lon = "";
    private String telephone = "";
    private String address = "";
    private List<String> images;
    private double rating;
    private String site = "";
    private LocalDate date;
    private LocalDate endDate;

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Event)) {
            return false;
        }

        Event other = (Event) o;
        return (this.name.equals(other.name) && this.address.equals(other.address));
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        //address = address.replaceAll(";", ",");
        this.address = address;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
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

       //name = name.replaceAll(";", ",");
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
      //  desc = desc.replaceAll(";", ",");
        this.date = date;
    }

    public void setEndDate(LocalDate date) {
        this.endDate = date;
    }

    public Event() {
    }

    public Event(String address, String desc, String category, List<String> images, String lat, String lon, String name,
            double rating, String telephone, String site, LocalDate date) {
        //name = name.replaceAll(";", ",");
       // desc = desc.replaceAll(";", ",");
       // address = address.replaceAll(";", ",");

        this.address = address;
        this.desc = desc;
        if (images.size() > 7) {
            images = images.subList(0, 7);
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
        this.date = date;
    }

    public String toString() {

        name = Jsoup.parse(name).text();
        desc = Jsoup.parse(desc).text();
        address = Jsoup.parse(address).text();
        
        name=name.replace(";", ",");
        desc=desc.replace(";", ",");
        address=address.replace(";", ",");

        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        //name description website latitude longitude address image_links rating
        sb.append("\""+name + "\", \"" + desc + "\", \"" + site + "\", \"" + lat + "\", \"" 
                + lon + "\", \"" + address + "\", \"");
        if (images != null) {
            for (String image : images) {
                if (!(image.contains("http://") || image.contains("https://"))) {
                    sb.append("http://").append(image).append(",");
                } else {
                    sb.append(image + ",");
                }
            }
            sb1.append(sb.toString().substring(0, sb.toString().length() - 1));
        }
           else sb1.append(sb);

        int a = (int) Math.round(rating);

        if (rating == -1||rating == 0) {
            if (endDate != null) {
                sb1.append("\", \"\", \"" + date + "\", \"" + endDate + "\"\n");
            } else {
                sb1.append("\", \"\", \"" + date + "\", \"" + date + "\"\n");
            }
        } else {
            if (endDate != null) {
                sb1.append("\", \"" + a + "\", \"" + date + "\", \"" + endDate + "\"\n");
            } else {
                sb1.append("\", \"" + a + "\", \"\", \"" + date + "\", \"" + date + "\"\n");
            }
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
