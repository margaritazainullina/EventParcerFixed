/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ith.eventparcerfx1.spots;

import com.ith.eventparcerfx1.Establishment;
import com.ith.eventparcerfx1.Event;
import com.ith.eventparcerfx1.MainApp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Margarita
 */
public class GoogleMapsUtil {

    final static Logger logger = Logger.getLogger(GoogleMapsUtil.class);
    static int failures = 0;

    public static String key = "AIzaSyBCNkuefQVFtOrrOauwAiBwn-mhzST3HI4";

    public static String[] keys = {
        "AIzaSyBCNkuefQVFtOrrOauwAiBwn-mhzST3HI4",        
        "AIzaSyDbNEulFug7u7ULzpsNi5-Uv2DOATCCBc0",
        "AIzaSyDQH3j6AFvmWltqc_m2XdkrV-TwC1wlEQY",
        "AIzaSyCl3-ltMjV7x9h4GjthqWpSEf24nAa0JGg",
        "AIzaSyCZBnjgsZZxVZ7PK1_esPo87fjs8jAhWHs",
        "AIzaSyCmPBt0TSs-IaOJIWBZ8mo14DiDcW1Adm8",
        "AIzaSyAIU71-UeMzlk-d510WSyQg7MyNScq76lI",
        "AIzaSyA2nu7zcLnhFuZqtJtQAbyj0U7O5FnUtVI",
        "AIzaSyBiqJIdFc5y7hJQXYJWk5dZ5acARbjBgAU",
        "AIzaSyCzpBqADNKsFF1tQp2B515hoZWY5bpnkNM",
        "AIzaSyA4s2ohvfTthra62AQYCraB4iRJJBLnyGQ",
        "AIzaSyB6FCn1h5HsEXNHLmynvXJZSCmQ8ceu5QM",
        "AIzaSyCOhGkKgMEmTLB7eSItUhyEcmMOYrIZfPg",
        "AIzaSyD1XLffgD1Bx4HCvLuSVw6iYUzSNlcRgG8",
        "AIzaSyCT7Taf5YcwR-zKMTAW2PSQb_cXxop9tP0",
        "AIzaSyB8LSiZoGPun7wlSkt_ZnDjXHsYM9Rf9Cs",
        "AIzaSyBCyvw1SppUTyblXpFMTp0ASsgT_nWaEww",
        "AIzaSyAGvLR-9Z5HsTKf0l9kD_ncfO36yYfS3Bo",
        "AIzaSyBODnam4_Cic2ChP7tz7UsXE-NjImALTjs",
        "AIzaSyCyfikj3Fqq5NcOWxjXEeR-3Ow71VJxzPE",
        "AIzaSyCRKJYhAKXfbgCh3D0a_y6V8MCHt2tLHEk",
        "AIzaSyDJl4E3YbCUkDQwnpoM8qhYZCcvZ2jWYEA",
        "AIzaSyDRE-cwuh28GVYXgc0Efs1QytVX6E4B7no",
        "AIzaSyDJxUNjAQX0dK-EQZ6kr91Z_rhcvaXIaUQ",
        "AIzaSyD0uyyRjJ6h1huQmcb-TU4XZFkf4Lod4qk",
        "AIzaSyB8sgRxWuLG5_7A41RsejiTi7RDkiCFFjw",
        "AIzaSyC9YRdAgUgiLqjPSTbLteUSSmNwz81mcik",
        "AIzaSyDYSsQk2T3xxZDr54Z1EWfWQImCBWxTEj8",
        "AIzaSyBerT2aV7qfRv1xB9aXgnY-OFQL8E_h7M8",
        "AIzaSyDk52IyhMQ526_yAugP8H7HPQOqGl8pn3o",
        "AIzaSyAvAfHG_PZ9WRiLBdwSu9hFsuTNcl4eS7c",
        "AIzaSyCHLmBKkhVFOAjIHNTlsae6TjfnFmxAf4A",
        "AIzaSyDvSjB_3sfi1PXmg5_kNLyGYzr6MfZ1mT8",
        "AIzaSyAfLYrroP_GHdWq957MDZqHmbBOy_gYXNg",
        "AIzaSyAYZlOckkpitqsGLFu49eHq9R0EGrjiGPg",
        "AIzaSyBj9gSC0Q7Hg-l0DSCYgYjpBZbsD7LtB00",
        "AIzaSyAhmyijJ8YPU6LXJRCxpXkFSbDbMCjZlXA",
        "AIzaSyDNPJdPdnfJtqq-MiDbAtLE4vapQnGLbhk",
        "AIzaSyB8ijhPRpmq5im7YBqeFnOGe-uVRWIeLlA",
        "AIzaSyB1vsupQJiLMbWJbiTRkUb42EncsXKIEEE",
        "AIzaSyAK5P__363LyPiQ0YlIBCN3402wIpjXLsY",
        "AIzaSyAOZxdHoZSAQt5mckKp6RkegUKMwgIAHsc",
        "AIzaSyDC9q0ISKfiXQEa5TrhxWMJpLBhcTRDOQQ",
        "AIzaSyBt_5TipMqgWRxB1KbNnJdlPtaAlrOmDFw",
        "AIzaSyDOyXRu0MT1JTC0WcwS7BW4XICRH0mmgeA",
        "AIzaSyBMGQY3NpEdfpEET2_fSh1i7z5xy5DVfsg",
        "AIzaSyDf5B6yoSpLnrqGimz8DaZI7PBegA5ZMjY",
        "AIzaSyCDw9ZRO3GvlNk5wipquf7LnLOOqVs_VLs",
        "AIzaSyCNj8XpiMU56FkUd1n5az8AiTE0sxZCYOw",
        "AIzaSyD4aN4OhfHkv1PbzcvrmBv7HsKZ4QDSb7c",
        "AIzaSyD4hiCWW-LT44QyDCj6cpz2EOcPGhdaXSQ",
        "AIzaSyA_bzQypi2BtJj6pBAC0TcFSZQvCj8OZK0",
        "AIzaSyBSYt6uKwnlKIO9RKqotjZayFHWqVz3I4g",
        "AIzaSyDEerwN5dPGaq5glpvwXbqkTsM9A19rrYA",
        "AIzaSyB6h_QHAEnibea_bHPnQ-ipqEjyH64Ou8Y",
        "AIzaSyBkv0pqP1daCWVZBVPK2F_y33YGfQcURtA",
        "AIzaSyDm8r3jbyfFVDEQneZDBWJnQsoj3TzrV5c",
        "AIzaSyAnWDwQuRgR1rq2jj_19AASTNi3Tx9Zovo",
        "AIzaSyAlDhOyzo8AQFRHl7NmD3-_91nOVaRg4U0",
        "AIzaSyCeROO8uSsrEXhOtxdu31g-vQ_nxLK7rF8",
        "AIzaSyDyDQ6wntGIPf7738Q0MZiJ9m26yoVIBQA",
        "AIzaSyCOhGkKgMEmTLB7eSItUhyEcmMOYrIZfPg",
        "AIzaSyD1XLffgD1Bx4HCvLuSVw6iYUzSNlcRgG8",
        "AIzaSyCT7Taf5YcwR-zKMTAW2PSQb_cXxop9tP0",
        "AIzaSyB8LSiZoGPun7wlSkt_ZnDjXHsYM9Rf9Cs",
        "AIzaSyBCyvw1SppUTyblXpFMTp0ASsgT_nWaEww",
        "AIzaSyAGvLR-9Z5HsTKf0l9kD_ncfO36yYfS3Bo",
        "AIzaSyBODnam4_Cic2ChP7tz7UsXE-NjImALTjs",
        "AIzaSyCyfikj3Fqq5NcOWxjXEeR-3Ow71VJxzPE",
        "AIzaSyCRKJYhAKXfbgCh3D0a_y6V8MCHt2tLHEk",
        "AIzaSyCXGo0ho_o1MZdxDdXD1j--ABjVZoNLzmE",
        "AIzaSyCYnwVey9GXQ3ZEZPKhySDRfJXYEjph_8Q",
        "AIzaSyANes-WTuVGHdcqw7PVBhAYrcN58Gwh47U",
        "AIzaSyAg4_ukKVplN3u7dlJd2bNKJAsReai75PI",
        "AIzaSyA4o3lnBUnkZP6Qr9aNVg7ofclEMj254No",
        "AIzaSyAgKN6GSsyryEQQPhnSnMsWb-_b-oYlxN0",
        "AIzaSyABhdfsItOy6ylzp4zPbupymrWus7M0Yh4",
        "AIzaSyARyBvUmVz9AIkGAMrZ2kqSeKEDMTJ-0zg",
        "AIzaSyDRb2HH0xupU3XUrZw6D6XzvsEShXlqxnI",
        "AIzaSyDGx5V2osP4s0ysSOL2gKs0zkgfFbpHoNs",
        "AIzaSyB_nPFaoqw42stGgrhgraCYc3cy3fuHBF0",
        "AIzaSyDjSoBePzRNWv9EOVTeXDPOlnWcQhpAdpY",
        "AIzaSyBT0rlYAEi0oUhePdX-pwE-xpxeGb8M10A",
        "AIzaSyApTFgiOSAHcSFjsQgKnY_bxI7QpHkNb7Q",
        "AIzaSyD1ekST5HghHIwx2xQQpqvPFHy7F-avSWk",
        "AIzaSyCAciUgGCXlj90BCAKBh3eIOAkgUYtVQOo",
        "AIzaSyDEUhH_37mnp7AmJ1A_sss2yqptPFPixYA",
        "AIzaSyAdiHEUw-ylwBtw0j4xx7SU7vzqj9ib5BI",
        "AIzaSyBGlI9BoO2hXUQu6YV_ajItksX1s7so93E",
        "AIzaSyA2Qd0AdHyA8WIQfbDwiXmdhYjtjbiZZU4",
        "AIzaSyDhO1oT_GlAuL3gJlCBcTXdrjZZfj-g-jM",
        "AIzaSyCPOkA3IaDloeWBWxTFrq-rY0M4cBCrB54",
        "AIzaSyCrQnRUnKgha_kUiQLDvhS9VJTYnx_J3no",
        "AIzaSyBPlCAkUDE-bTaMvbORayFqaU2a6-qbSho",
        "AIzaSyAjoB2NRkiNwnw7DoHVaW6M9Ed0CHtAiFQ",
        "AIzaSyCyROgFJrbvrpJZAAa7bRqHgl05XHnDocI",
        "AIzaSyD8oXHGqxCa1enaOfJdzeRqA_1HEHMFydw",
        "AIzaSyB1FuXYM02j4gtvMllVlXFZfORzfEkYi84",
        "AIzaSyB8yNmHOFairJUlk0QDOJAmahmmA_4-jnE",
        "AIzaSyCma9MZ2uvNrw-OuMyY49e8JDBb7_0qIAo",
        "AIzaSyA4EdliA9-vt57OwQvNJdAdaH7zh3F7s98",
        "AIzaSyBZI5Na-Z0MUvptq8Q5_S2zBeVvHkmv1S8",
        "AIzaSyA3J6mGQMu7Zhhy0qDuI11nZAFtnyKYcOk",
        "AIzaSyCeshTNdbLAtS2o5ETvw4EykskflRySaCY",
        "AIzaSyDRpKOsxNofE6waNTEkbcAV6bZ9UyuYA70",
        "AIzaSyAdYr235nn32Kcpfmpcxa82iIWVCT_R0N0",
        "AIzaSyCcA0IIN6UjtxHp98eVz0iqcuZ2oKoAkBw",
        "AIzaSyAiYkXqsTIDaR6pfUV_5BKCCTIcmqnFS_s",
        "AIzaSyCLKpQMy3Mn6us2eXFEpAem20rHaZYcKRU",
        "AIzaSyBCAZYDkmSQedjEk1nAKMyR_pYYQpqlTZE",
        "AIzaSyA40PbcrHyHqhlTL2TRdy3zq9MNVY38jLs",
        "AIzaSyBbrtkl2NnZj-0PIVkBEvJmR--hUiAQDbc",
        "AIzaSyBdO61ALE8EKvJGO9HAWnGKK0rQYEMFxP8",
        "AIzaSyAer56MgBm8mUWEx0eKjwKTxQ9GCtPIZ80",
        "AIzaSyCH6QUIQYSGsq23fAXA_tn5pUxClUJ25vs",
        "AIzaSyAbivzzGxW2Gr3Noz_pPhSV79k-htnYn-o",
        "AIzaSyCB9e8v_THsmAgF8pYyIGtqEszX45xJns8",
        "AIzaSyAoZeR2vZoe9aXRbOJ1_q0zRSPtT7NFVVc",
        "AIzaSyBb3QEquaSWyCBi2UWI8NwFCFSQvygtWfc",
        "AIzaSyDTBdQ2474qTnT_a3IN06I5Ja6LikVLZJU",
        "AIzaSyAH0GumnXjCGfx-1-Jbh4XeSGCJqgAgqPc",
        "AIzaSyA4_U4oP5ashTTG5EA6CluNStrmYNe2M9E",
        "AIzaSyCC0YYKq5rWlwJY_cLDyODASvQ-LjTykzA",
        "AIzaSyA8abIxq8GbSq5sAjCaW8JjOcpPkUg52Hk",
        "AIzaSyDpUjpRmvqhKNMsW5-_Oi5Nsp7ei-U5PXU",
        "AIzaSyCXxC7ALQL04IpmrERozOxbzR_7-lYGy50",
        "AIzaSyDT6oMjXDJcINrd9AT1stcacAGvWRpKLMI",
        "AIzaSyClPBQQuCweaGBRzrXx4Mg735bKhb7lqss",
        "AIzaSyC2RcvrNLUWcDkyTK7rtGlgRlBa17HoFGo",
        "AIzaSyB45oUoYYOz95z_NUJWNg3HfhqeiYxhpgw",
        "AIzaSyB2wNNBNpMHb4PeyRsbsnLAL1Ur2Hbioyo",
        "AIzaSyANjCetxnnhV89eoaeKCok5kKcAM7n-FHo",
        "AIzaSyDNd9J5W-OAgx1V0R4YV8-1KB9GjRl3F6I",
        "AIzaSyACs4MSOjkwy5X_H0G97uOfF7U3vMvdrTY",
        "AIzaSyBRQgerOFhi8HiPqQB1K8OZg3Y1EaRz9c0",
        "AIzaSyBSSLAd56N1mG772eA_CuqLaoHtEKao3PU",
        "AIzaSyCKShn91Pda3yTxhQBVu7-_1LaR3MxnZ9M",
        "AIzaSyDgOkZlymJVctkvYpHYsRwIRCW0CPBrKA8",
        "AIzaSyDCNJSHSWZyk9qHNmFZz7EInvUL0Dxu8Z0",
        "AIzaSyDtHM0RdJLLSE1_fVwu0plnlARrpxv-RMw",
        "AIzaSyAessyy1ASJOmP2VH6dx52EV8GyjMtDlvg",
        "AIzaSyCmkx2N6_aynL9CrNfL8VfuSpYaoBb79Dw",
        "AIzaSyDwXV6BayKpLYOyGJZ3evlYMzbfBjT4pXY",
        "AIzaSyAhJ-95f5xd6eoW17IXxmOBQGhkZfxA1ao",
        "AIzaSyCKkg6MNjNfm__5ywkcFsGE-Z480O7dEbo",
        "AIzaSyCXKu7AC4owMv3fj0JDcnHzQuE9jT66Dds",
        "AIzaSyAGUZvulm5_zAdYPj_FjrU8kD2nXqvcsi4",
        "AIzaSyDgRYJDuv583QGDsO69k1MR2kotkOWeG64",
        "AIzaSyD7p9q19U_1y_FaCGNbzKoroyUBnWuUgIY",
        "AIzaSyBARLoAsmVEUBOvtc1X5m-6VLL6hTOgddc",
        "AIzaSyC43cFtIfwTJ0mdJTFzMlWtN8U3_E_JbIk",
        "AIzaSyDmE1K0UgqeGAqyEaZ0k4TAuReGOibTD54",
        "AIzaSyBuOpLovUzWAY3GO0HLlpCkGHUPDZKk3rs",
        "AIzaSyDsyw2RBNLPivSsQAeJQ_9gHtUjgObysI4",
        "AIzaSyA-NHHn9J9UTczS4elHfKoirhcm2DCh5fA",
        "AIzaSyCJcQpXMGQ80WzzRyPdBQPFwJPitxIftGQ",
        "AIzaSyBYa4pPrXvjW4eAgPi6GvTK6E0Yq5yQs2Q",
        "AIzaSyC9TxR7tQQjA1tW6Px4Raf19Sc1NyulLB4",
        "AIzaSyBKhkeDNwaOm3jHx8IzKPBIKz0KQOqFy1c",
        "AIzaSyBnOY86LFbHy3xhzp99qbkUfFCYxEQR6I4",
        "AIzaSyBcygJ6Cpc3WySR913Ly4bUjU3UBQIT23g",
        "AIzaSyAFlS3OuQDUZ-0SwHiy05fUlxMI6QO2kfY",
        "AIzaSyC3DwV3hFNx-GZVaKWAc6w6ku8kjxxQfFM",
        "AIzaSyCckWkDA1X0-mlHX7w7AbCoX75OrY1JDHg",
        "AIzaSyDUBCzezoSmxOGjg7vIEARWri-UtSWGQ28",
        "AIzaSyCDDtOYyq-OMT2_1N3Ow8WFCqN_1W7JQrY",
        "AIzaSyDyRgOq3U_Tv8td_4d7dzpTCcPajqePcq0",
        "AIzaSyDy7f31l4kMHojB7ForV1ar6GCiJ3SdhMc",
        "AIzaSyA3OZ-2DF6ptirrjvsahiUasXGxEgypn2I",
        "AIzaSyA8sPZU2mQ8Sz01JDK-UhbMBfVuHerPCpA",
        "AIzaSyBT7IL_J0oPIJV1fgFu6emTMtZe_70VNBw",
        "AIzaSyB5ycnmIR8fX3MfeFeCnkYY-eJxm0HhfxY",
        "AIzaSyCxzBSZbANiN-dyxfIYHHMPo9VGpS2svic",
        "AIzaSyD68fmDnhQUpjv2plwEyvKGvifYjPurQQM",
        "AIzaSyAXKIJ0XnnVPmlfrjhS6JTSV4ZCA9bIAXg",
        "AIzaSyBFtdyEAIwyhUs7FxmsAX3XUO_RiChGVX8",
        "AIzaSyBkSu4MqKIaijBgOtYgqKLpGEeIq0kzzP4",
        "AIzaSyAK5RRUn93hOZwTj9F46HHXVaaMuyiSkPw",
        "AIzaSyBZK_PoTtZM_TspSP0CJdTWayVNd9gFROI",
        "AIzaSyBGzeiL5lB_Jcp8MVXv583C_a2A7XL7AZw",
        "AIzaSyDRgUnUPm2SfHagY-8dNugwKHX9Wu715rM",
        "AIzaSyBicSizclwkjA5hU0WWIv1Uw7l4YvdaPAQ",
        "AIzaSyAB001_4odvODgBjnKYfSwgJi9EkQnsnzM",
        "AIzaSyCeM2aIr0_PFUE3xBgRry423_-24beHFKI",
        "AIzaSyBGWyV76lbfB4BbozXVLeZOEE6CwRvFlTg",
        "AIzaSyAvEFL3MY8lZsHxmKAkVj0NQ8BKqxYbToA",
        "AIzaSyBUt0S4Y1pCOZGi8Kqa5qOjJZF4GtwGvVg",
        "AIzaSyAw4nhJwDbTtKmFPwyK1-Xm2QnAyiX5rPQ",
        "AIzaSyA6bOlXgSuhY05yo3QCdrg0Zs2ksOPQDmE",
        "AIzaSyBOiur9OJ4AMPpki3rDPnNmLSnLRwAlb-o",
        "AIzaSyAPSHmT5ptbhnRYsMKHR_NO-21Wvf_8948",
        "AIzaSyCZsKC-x0aQWzeI-bfva2jIE6wTsEdLFB8",
        "AIzaSyAzNIc65PQdxUASDXvIaRH2-IBEgsa8ew0",
        "AIzaSyBS5QYsh4mfFA29JMylTYrlODMsA69gSdE",
        "AIzaSyB55ER4weE_4trpJDvtoBCJJDawu_e-uYU",
        "AIzaSyDSN0jURrVpOVAabWn-ABwMns9dqa82nuM",
        "AIzaSyBhLluW5z_qT8Hm_mg7xOH5xxO2iT4XWk4",
        "AIzaSyBkuYSlmIGJfqxpldMgFGrMtgoDsZcKUPU",
        "AIzaSyC9F4ne3RiNPfcigkCz8Txmdku11ZWyY7s",
        "AIzaSyCh7_wTLZdhqsHmSJgSPBJha_1jZCtuLy8",
        "AIzaSyBeARIHdlvtFJIMQ3Rnk_h63jbveOE-Vn4",
        "AIzaSyDZE38JxIZ2OTx793gDXa0-MILGFXnTl_0",
        "AIzaSyAAv3pEr6E7kQ_7xfC53epLJ7gqtMLRDBU",
        "AIzaSyBQ-aN6CfCRc6UinhyNtoM5PbdihtBxtJk",
        "AIzaSyC46KPYx0NPhT7vmrhoh7Fnk7bkznrX5v4",
        "AIzaSyBnwBKr2ru9tVqxSxwpXn1ZsmV-6mrKFDw",
        "AIzaSyDHPYILWB_37ACy9RKGSgMQK02E39QqGS0",
        "AIzaSyCPTzjJZUG_yteP2IDhXOCH-XnqyRXkD0g",
        "AIzaSyDuE7yhRE_AST9o1m_Eg004PpB8-UzWYDA",
        "AIzaSyCJJQooprMXY02UsqyfLxfdm1HR-ehXCXs",
        "AIzaSyAsj-yVY86vxKlUQ0TUGr20wQKK9nIR988",
        "AIzaSyDxnEPyvwwIQqwog9l4X1f8ucXA4Adgjjo",
        "AIzaSyB04CMP_jsh99QgdU-ZK3qnbp6zjaygAbU",
        "AIzaSyCghUhMs2Rrm0DcTy4vYSX2ZiNsCSNq3tg",
        "AIzaSyAH7SlHzZ4HNpAruflf4kQIVn7tmNTfLYE",
        "AIzaSyAH7SlHzZ4HNpAruflf4kQIVn7tmNTfLYE",
        "AIzaSyBhiBgXIXaoN2QZ5inXdzlWDje38oCc1Ps",
        "AIzaSyC-So7NPqLtsoXoHZRo1Hl6mUnGhuNZsrM",
        "AIzaSyCmGGmEzHM3a0jS5YfE0FMT1Z1yPaDC1Io",
        "AIzaSyBMZaS-YLtMyAnUEfZFlf23IS-YB2xW-Wo",
        "AIzaSyDUGKIpfF62rr0TOtDweitgpQ4J2H3Cqgk",
        "AIzaSyD1_UOsrTR6zkPyzrM3eMVuyK2htj6Zyhg",
        "AIzaSyC-ws7WhRq1DoPUggAnjmOLqAeat_W0Bco",
        "AIzaSyCTQGDkE3sWsNWcchZqT2OKNUTM-h86v7c",
        "AIzaSyBBqG7dun0V93hsCIi6BMMdH_qjZ8XBXvU",
        "AIzaSyDEM69MlS3Om0_jv4dY9rZiarmAjtzaNJU"
    };

    public static Establishment addCoordinatesInfo(Establishment e, String city) throws UnsupportedEncodingException, MalformedURLException, IOException {
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + URLEncoder.encode(e.getAddress(), "UTF-8") + "&sensor=false&key=" + key;
        URL objUrl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) objUrl.openConnection();
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        StringBuilder json;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            json = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
        }

        JSONObject obj = null;
        try {
            obj = new JSONObject(json.toString().trim());
            MainApp.quotaUsed++;
            try {
                if (obj.getString("error_message") != null) {
                    System.err.println(obj.getString("error_message"));
                    if (!isQuotaExceeded()) {
                        switchCredentials();
                        addCoordinatesInfo(e, city);
                    } else {
                        return null;
                    }
                }
            } catch (JSONException ex) {
            }

            JSONArray results = obj.getJSONArray("results");
            JSONObject address = (JSONObject) results.get(0);
            String formattedAddress = address.getString("formatted_address");
            e.setAddress(formattedAddress);

            for (int i = 0; i < results.length(); i++) {
                JSONObject result = (JSONObject) results.get(i);
                JSONObject geometry = result.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                String lat = location.getString("lat");
                String lon = location.getString("lng");
                if (formattedAddress.contains(city)) {
                    e.setLat(lat);
                    e.setLon(lon);
                    return e;
                }
            }

        } catch (JSONException ex) {
            return e;
        }
        return e;
    }

    public static Event addCoordinatesInfo(Event e, String city) throws UnsupportedEncodingException, MalformedURLException, IOException, Exception {
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + URLEncoder.encode(e.getAddress(), "UTF-8") + "&sensor=false&key=" + key;
        URL objUrl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) objUrl.openConnection();
        int responseCode = con.getResponseCode();

        if (responseCode != 200) {
            logger.error("Google Maps API: returned code " + responseCode);
        }

        StringBuilder json;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            json = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
        }

        JSONObject obj = null;
        try {
            obj = new JSONObject(json.toString().trim());
            MainApp.quotaUsed++;

            try {
                if (obj.getString("error_message") != null) {
                    logger.error(obj.getString("error_message"));
                    switchCredentials();
                    if (isQuotaExceeded()) {
                        throw new Exception();
                    }
                    addCoordinatesInfo(e, city);
                }
            } catch (JSONException ex) {
            }

            JSONArray results = obj.getJSONArray("results");
            JSONObject address = (JSONObject) results.get(0);
            String formattedAddress = address.getString("formatted_address");

            for (int i = 0; i < results.length(); i++) {
                JSONObject result = (JSONObject) results.get(i);
                JSONObject geometry = result.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                String lat = location.getString("lat");
                String lon = location.getString("lng");
                if (formattedAddress.contains(city)) {
                    e.setAddress(formattedAddress);
                    e.setLat(lat);
                    e.setLon(lon);
                    return e;
                }
            }

        } catch (JSONException ex) {
            return e;

        }
        return e;
    }

    public static String getAddress(String lat, String lon) throws UnsupportedEncodingException, MalformedURLException, IOException {
        String query = lat + "," + lon;
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + URLEncoder.encode(query, "UTF-8") + "&key=" + key;
        MainApp.quotaUsed++;
        URL objUrl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) objUrl.openConnection();
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            logger.info("Google Maps API: response code " + responseCode);
        }

        StringBuilder json;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            json = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
        }

        JSONObject obj = null;
        try {
            obj = new JSONObject(json.toString().trim());
            MainApp.quotaUsed++;
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(GoogleMapsUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            if (obj.getString("error_message") != null) {
                logger.error(obj.getString("error_message"));
                switchCredentials();
                getAddress(lat, lon);
            }
        } catch (JSONException ex) {
        }

        try {
            JSONArray results = obj.getJSONArray("results");
            JSONObject address = (JSONObject) results.get(0);
            String formattedAddress = address.getString("formatted_address");
            logger.info("Google Maps API: lat-" + lat + ", lon-" + lon + ", address - " + formattedAddress);
            return formattedAddress;
        } catch (JSONException ex) {
            return "";
        }
    }

    private static void switchCredentials() {
        failures++;
        for (int i = 0; i < keys.length; i++) {
            logger.info("Google Maps API: trying another key...");
            System.out.println(key);
            if (key.equals(keys[keys.length - 1])) {
                key = keys[0];
            }
            if (key.equals(keys[i])) {
                key = keys[i + 1];
                return;
            }
        }
    }

    public static boolean isQuotaExceeded() {
        return (failures >= keys.length + 1);
    }
}