# Domain Encounter — Analisis

## 1. Apakah sebuah encounter merupakan bagian dari sebuah visit, atau sebuah visit merupakan bagian dari sebuah encounter?

**Jawaban:**  
**Encounter adalah bagian dari visit.**

**Alasan:**  
Dalam skema database OpenMRS, foreign key berada pada tabel **`encounter`**, yaitu:


Ini berarti:

- Satu **visit** dapat berisi **banyak encounter**.  
- Karena itu, encounter termasuk di dalam visit, bukan sebaliknya.

Sehingga interpretasi yang benar adalah:

**Encounter ⟶ bagian dari Visit**  
(Visit adalah entitas induk, encounter adalah entitas anak di dalam visit)

---

## 2. Bagaimana provider terhubung dengan person?

**Jawaban:**  
Provider terhubung dengan person melalui kolom `person_id` pada tabel **`provider`**.


**Penjelasan:**  
- Setiap provider di OpenMRS sebenarnya adalah seorang **person** yang memiliki peran klinis.  
- Informasi demografis (nama, jenis kelamin, tanggal lahir, dll.) disimpan di tabel **person**.  
- Tabel provider menambahkan informasi khusus provider, seperti identifier, role, dan metadata.  

Dengan demikian:

**Provider** adalah **person** yang diberi atribut tambahan sebagai penyedia layanan,  
dihubungkan melalui **`provider.person_id` → `person.person_id`**.

