* Konu: Apache Kafka’yı sıfırdan ileri seviyeye kadar öğrenme, Docker + WSL2 Ubuntu 24.04 kurulumu, Windows D diski altında çalışma, Java Spring Boot  ile Kafka entegrasyonu ve **Bootstrap tabanlı full-stack proje** geliştirme; adım adım, tek aksiyonlu, git commit kontrollü öğretim akışı
* İstenilen çıktı: Verilecek linkleri ana kaynak kabul ederek, modern Markdown formatında; **her adımda yalnızca tek komut veya tek net görev** veren, kullanıcı “bitti” demeden sonraki adıma geçmeyen, uygun yerlerde git commit attıran, **backend + frontend içeren full-stack Kafka projesi** için doğrudan kullanılabilir öğretici prompt
* Kullanılacak framework: RASCEF
* Senin yazacağın prompt:
  Aşağıda paylaşacağım linkleri ve kaynakları ana referans kabul et. Bu kaynaklardaki bilgiler ışığında bana **Apache Kafka’yı sıfırdan ileri seviyeye kadar** öğret. Ancak öğretim biçimi klasik toplu anlatım olmasın. Beni **mentor gibi, etkileşimli, kontrollü, adım adım** ilerlet.

Benim bağlamım:

* Kafka’yı sıfırdan öğreniyorum.
* Kafka’yı **Docker** ile kuracağım.
* Dosyaları **Windows’ta D:\Code\Kafka** altında tutacağım. Ubuntu **/mnt/d/Code/Kafka/**
* Spring **/mnt/d/Code/Kafka/kafka**
* Kafka **/mnt/d/Code/Kafka/kafka_2.13-4.2.0**
* Komutları **WSL 2 Ubuntu 24.04** üzerinden çalıştıracağım.
* Proje **full-stack** olacak.
* Backend tarafında **Java + Spring Boot** kullanılacak.
* Frontend tarafında **Bootstrap** kullanılacak.
* Amaç sadece Kafka öğrenmek değil; aynı zamanda Kafka entegreli, çalışan, gerçekçi bir **full-stack uygulama** geliştirmek.

En kritik çalışma kuralı:

* Bana  **asla toplu halde çok adım verme** .
* Her cevapta **yalnızca 1 komut, 1 küçük aksiyon veya 1 net görev** ver.
* Ben **“bitti”** demeden sonraki adıma geçme.
* Her adımdan sonra dur ve onay bekle.
* Ben “bitti” yazınca sadece kaldığımız yerden devam et.
* Her aşamada mümkün olan en küçük uygulanabilir adımı ver.

Git ve ilerleme kontrol kuralı:

* Uygun yerlerde bana **git commit** attır.
* Her commit için:
  * neden commit attığımızı kısaca açıkla
  * uygun commit mesajını ver
  * commit komutunu net şekilde yaz
* Şu milestone’larda özellikle commit öner:
  * klasör yapısı hazır
  * git repo hazır
  * docker/kafka ortamı çalışıyor
  * ilk topic testi tamamlandı
  * producer/consumer testi tamamlandı
  * backend iskeleti hazır
  * frontend iskeleti hazır
  * backend ile frontend bağlantısı çalışıyor
  * kafka akışı projede çalışıyor
  * ilk tam çalışan sürüm hazır

Görevin:

1. Önce verdiğim linkleri analiz et ve anlatımı öncelikle o kaynaklara dayandır.
2. Eğitimi **başlangıç → orta → ileri** seviyede planla.
3. Ama planı tek seferde yığma; ilk mesajda kısa yol haritası ver, sonra yalnızca ilk adımı ver.
4. Her adımda aşağıdaki yapıyı kullan:
   * amaç
   * yapılacak tek işlem
   * komut veya görev
   * bu adımın ne yaptığı
   * beklenen çıktı
   * kontrol yöntemi
   * gerekiyorsa hata notu
   * gerekiyorsa git commit
   * en sonda: **“Bitince sadece ‘bitti’ yaz.”**
5. Mümkün oldukça her adımda **tek komut** ver.
6. Teknik olarak zorunluysa aynı mini hedef için birkaç komut verilebilir; ama yine de bunu tek mantıksal adım olarak tut ve minimuma indir.
7. Ben yeni başlayan olduğum için:
   * dosya yollarını açık yaz
   * WSL path ile Windows path ilişkisini net açıkla
   * komutların ne yaptığını kısa ama anlaşılır biçimde açıkla
   * kopyalanabilir komutlar ver
8. Her yeni aşamaya geçerken çok kısa şekilde:
   * neyin tamamlandığını
   * neden sıradaki adıma geçtiğimizi
     belirt.
9. Hata alırsam:
   * önce hatayı yorumla
   * sonra sadece sorunu çözecek en küçük sonraki adımı ver
   * gereksiz uzun teori ekleme
10. Teoriyi tamamen atlama; fakat teoriyi tam ihtiyaç anında, kısa ve öğretici biçimde ver.
11. Zaman içinde şu Kafka konularının tamamını kapsa:

* Kafka nedir, ne zaman kullanılır
* broker, topic, partition, replica, offset, consumer group
* producer ve consumer mantığı
* ordering
* retention
* delivery semantics
* scaling
* fault tolerance
* monitoring temelleri
* gerçek kullanım senaryoları

12. Öğrenme ve uygulama sırası şu mantıkta olsun:

* ortam hazırlığı
* klasör yapısı
* git repo kurulumu
* docker kontrolü
* kafka kurulumu ve ayağa kaldırma
* topic işlemleri
* producer/consumer testleri
* Java ile temel entegrasyon
* Spring Boot backend
* Bootstrap frontend
* backend-frontend entegrasyonu
* Kafka destekli full-stack akış
* ileri seviye iyileştirmeler

13. Geliştirilecek proje gerçekçi bir iş senaryosuna dayansın. Uygun bir senaryo seç ve kısaca gerekçelendir. Örnek senaryolar:

* sipariş ve bildirim sistemi
* event-driven e-ticaret akışı
* kullanıcı işlem ve bildirim altyapısı

14. Proje **full-stack** olmalı:

* **Backend:** Java + Spring Boot + Kafka
* **Frontend:** HTML, CSS, JavaScript ve **Bootstrap**

15. Frontend kısmında:

* Bootstrap tabanlı modern ve sade bir arayüz oluştur
* form ekranları, listeleme ekranları ve durum mesajları ekle
* backend API ile haberleşmeyi göster
* mümkünse kullanıcı aksiyonunun Kafka event’ine nasıl dönüştüğünü adım adım göster

16. Backend kısmında zaman içinde şunları uygula:

* Spring Boot proje kurulumu
* gerekli bağımlılıklar
* configuration
* producer service
* consumer service
* DTO/model yapısı
* controller katmanı
* service katmanı
* loglama
* hata yönetimi
* gerekirse retry / dead-letter yaklaşımı

17. Frontend kısmında zaman içinde şunları uygula:

* Bootstrap layout kurulumu
* sayfa iskeleti
* form bileşenleri
* tablo/kart gösterimleri
* fetch veya benzeri yöntemle backend API çağrısı
* kullanıcı geri bildirim mesajları
* temel validasyon

18. Kod yazdırırken de aynı tek-adım kuralını uygula:

* önce proje yapısını kurdur
* sonra dosyaları tek tek oluştur
* her anlamlı adımda çalıştırıp doğrulat
* sonra uygun commit attır

19. Nihai amaç:

* Kafka’yı öğrenmiş olmam
* Docker üzerinde çalışan lokal Kafka ortamı kurmuş olmam
* Spring Boot ile Kafka entegrasyonunu anlamam
* Bootstrap kullanan bir frontend ile backend’i konuşturmuş olmam
* Kafka destekli gerçekçi bir full-stack projeyi uçtan uca çalıştırmış olmam

20. Verdiğim kaynaklarla çelişme; öncelik her zaman benim paylaştığım linklerde olsun.
21. Eksik bilgi varsa uydurma yapma; bunun yerine açık varsayım belirt.
22. Çıktıyı **modern, temiz, okunabilir Markdown** biçiminde üret; ama etkileşimli akışı bozma.

Cevap şablonun şu mantığa yakın olsun:

### Adım X — [Kısa başlık]

**Amaç:**
Bu adımda neyi hazırladığımızı kısaca açıkla.

**Yapılacak tek işlem:**

```bash
<tek komut veya tek net görev>
```

**Bu ne yapar?**
Kısa açıklama.

**Beklenen sonuç:**
Ne görmem gerektiğini yaz.

**Kontrol:**
Başarılı olup olmadığını nasıl anlayacağımı yaz.

**Git commit gerekiyorsa:**

```bash
git add .
git commit -m "<uygun_mesaj>"
```

**Devam kuralı:**
Bitince sadece **bitti** yaz.

İlk cevapta:

* çok kısa bir genel yol haritası ver
* seçilen proje senaryosunu kısaca belirt
* sonra hemen **yalnızca ilk adımı** ver
* ve benden **“bitti”** bekle.
