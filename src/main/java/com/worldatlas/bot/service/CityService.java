package com.worldatlas.bot.service;

import com.worldatlas.bot.entity.City;
import com.worldatlas.bot.repository.CityRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@Slf4j
@Service
public class CityService {
    
    private final CityRepository cityRepository;

    private static final Map<String, String> EN_TO_RU = Map.ofEntries(
        Map.entry("moscow", "москва"),
        Map.entry("saint petersburg", "санкт-петербург"),
        Map.entry("st petersburg", "санкт-петербург"),
        Map.entry("novosibirsk", "новосибирск"),
        Map.entry("yekaterinburg", "екатеринбург"),
        Map.entry("kazan", "казань"),
        Map.entry("nizhny novgorod", "нижний новгород"),
        Map.entry("samara", "самара"),
        Map.entry("chelyabinsk", "челябинск"),
        Map.entry("omsk", "омск"),
        Map.entry("krasnoyarsk", "красноярск"),
        Map.entry("voronezh", "воронеж"),
        Map.entry("perm", "пермь"),
        Map.entry("volgograd", "волгоград"),
        Map.entry("krasnodar", "краснодар"),
        Map.entry("saratov", "саратов"),
        Map.entry("tyumen", "тюмень"),
        Map.entry("tolyatti", "тольятти"),
        Map.entry("izhevsk", "ижевск"),
        Map.entry("barnaul", "барнаул"),
        Map.entry("irkutsk", "иркутск"),
        Map.entry("khabarovsk", "хабаровск"),
        Map.entry("vladivostok", "владивосток"),
        Map.entry("yakutsk", "якутск"),
        Map.entry("murmansk", "мурманск"),
        Map.entry("kaliningrad", "калининград"),
        Map.entry("arkhangelsk", "архангельск"),
        Map.entry("tula", "тула"),
        Map.entry("ryazan", "рязань"),
        Map.entry("astrakhan", "астрахань"),
        Map.entry("penza", "пенза"),
        Map.entry("lipetsk", "липецк"),
        Map.entry("kemerovo", "кемерово"),
        Map.entry("tomsk", "томск"),
        Map.entry("nalchik", "нальчик"),
        Map.entry("grozny", "грозный"),
        Map.entry("magnitogorsk", "магнитогорск"),
        Map.entry("sochi", "сочи"),
        Map.entry("rostov-on-don", "ростов-на-дону"),
        Map.entry("ufa", "уфа"),
        Map.entry("vorkuta", "воркута"),
        Map.entry("syktyvkar", "сыктывкар"),
        Map.entry("petrozavodsk", "петрозаводск"),
        Map.entry("maykop", "майкоп"),
        Map.entry("elista", "элиста"),
        Map.entry("cherkessk", "черкесск"),
        Map.entry("nazran", "назрань"),
        Map.entry("makhachkala", "махачкала"),
        Map.entry("vladikavkaz", "владикавказ"),
        Map.entry("saransk", "саранск"),
        Map.entry("yoshkar-ola", "йошкар-ола"),
        Map.entry("cheboksary", "чебоксары"),
        Map.entry("kirov", "киров"),
        Map.entry("veliky novgorod", "великий новгород"),
        Map.entry("pskov", "псков"),
        Map.entry("smolensk", "смоленск"),
        Map.entry("bryansk", "брянск"),
        Map.entry("oryol", "орёл"),
        Map.entry("kursk", "курск"),
        Map.entry("belgorod", "белгород"),
        Map.entry("tambov", "тамбов"),
        Map.entry("kostroma", "кострома"),
        Map.entry("ivanovo", "иваново"),
        Map.entry("vladimir", "владимир"),
        Map.entry("yaroslavl", "ярославль"),
        Map.entry("tver", "тверь"),
        Map.entry("volzhsky", "волжский"),
        Map.entry("nakhodka", "находка"),
        Map.entry("angarsk", "ангарск"),
        Map.entry("bratsk", "братск"),
        Map.entry("arzamas", "арзамас"),
        Map.entry("balashikha", "балашиха"),
        Map.entry("khimki", "химки"),
        Map.entry("mytishchi", "мытищи"),
        Map.entry("korolyov", "королёв"),
        Map.entry("odintsovo", "одинцово"),
        Map.entry("kolomna", "коломна"),
        Map.entry("serpukhov", "серпухов"),
        Map.entry("novocherkassk", "новочеркасск"),
        Map.entry("taganrog", "таганрог"),
        Map.entry("novorossiysk", "новороссийск"),
        Map.entry("yuzhno-sakhalinsk", "южно-сахалинск"),
        Map.entry("petropavlovsk-kamchatsky", "петропавловск-камчатский"),
        Map.entry("magadan", "магадан"),
        Map.entry("almaty", "алматы"),
        Map.entry("astana", "астана"),
        Map.entry("shymkent", "шимкент"),
        Map.entry("karaganda", "караганда"),
        Map.entry("aktobe", "актобе"),
        Map.entry("atyrau", "атрау"),
        Map.entry("pavlodar", "павлодар"),
        Map.entry("ust-kamenogorsk", "уст-каменогорск"),
        Map.entry("semey", "семей"),
        Map.entry("kostanay", "костанай"),
        Map.entry("kyzylorda", "кызылорда"),
        Map.entry("aktau", "актау"),
        Map.entry("taraz", "тараз"),
        Map.entry("turkistan", "туркестан"),
        Map.entry("balkhash", "балхаш"),
        Map.entry("jezkazgan", "жезказган"),
        Map.entry("kokshetau", "кокшетау"),
        Map.entry("taldykorgan", "талдыкорган"),
        Map.entry("oral", "уральск"),
        Map.entry("petropavl", "петропавловск"),
        Map.entry("kyiv", "киев"),
        Map.entry("kiev", "киев"),
        Map.entry("minsk", "минск"),
        Map.entry("tashkent", "ташкент"),
        Map.entry("bishkek", "бишкек"),
        Map.entry("dushanbe", "душанбе"),
        Map.entry("baku", "баку"),
        Map.entry("yerevan", "ереван"),
        Map.entry("tbilisi", "тбилиси"),
        Map.entry("chisinau", "кишинёв"),
        Map.entry("paris", "париж"),
        Map.entry("berlin", "берлин"),
        Map.entry("london", "лондон"),
        Map.entry("madrid", "мадрид"),
        Map.entry("rome", "рим"),
        Map.entry("amsterdam", "амстердам"),
        Map.entry("vienna", "вена"),
        Map.entry("stockholm", "стокгольм"),
        Map.entry("helsinki", "хельсинки"),
        Map.entry("oslo", "осло"),
        Map.entry("copenhagen", "копенгаген"),
        Map.entry("lisbon", "лиссабон"),
        Map.entry("athens", "афины"),
        Map.entry("istanbul", "стамбул"),
        Map.entry("warsaw", "варшава"),
        Map.entry("prague", "прага"),
        Map.entry("riga", "рига"),
        Map.entry("vilnius", "вильнюс"),
        Map.entry("tallinn", "таллин"),
        Map.entry("tokyo", "токио"),
        Map.entry("osaka", "осака"),
        Map.entry("beijing", "пекин"),
        Map.entry("shanghai", "шанхай"),
        Map.entry("seoul", "сеул"),
        Map.entry("bangkok", "бангкок"),
        Map.entry("dubai", "дубай"),
        Map.entry("singapore", "сингапур"),
        Map.entry("delhi", "дели"),
        Map.entry("mumbai", "мумбаи"),
        Map.entry("cairo", "каир"),
        Map.entry("johannesburg", "йоханнесбург"),
        Map.entry("sydney", "сидней"),
        Map.entry("auckland", "окленд")
    );

    private static final Map<String, String> RU_TO_EN = Map.ofEntries(
        Map.entry("москва", "Moscow"),
        Map.entry("санкт-петербург", "Saint Petersburg"),
        Map.entry("новосибирск", "Novosibirsk"),
        Map.entry("екатеринбург", "Yekaterinburg"),
        Map.entry("казань", "Kazan"),
        Map.entry("нижний новгород", "Nizhny Novgorod"),
        Map.entry("самара", "Samara"),
        Map.entry("челябинск", "Chelyabinsk"),
        Map.entry("омск", "Omsk"),
        Map.entry("красноярск", "Krasnoyarsk"),
        Map.entry("воронеж", "Voronezh"),
        Map.entry("пермь", "Perm"),
        Map.entry("волгоград", "Volgograd"),
        Map.entry("краснодар", "Krasnodar"),
        Map.entry("саратов", "Saratov"),
        Map.entry("тюмень", "Tyumen"),
        Map.entry("тольятти", "Tolyatti"),
        Map.entry("ижевск", "Izhevsk"),
        Map.entry("барнаул", "Barnaul"),
        Map.entry("иркутск", "Irkutsk"),
        Map.entry("хабаровск", "Khabarovsk"),
        Map.entry("владивосток", "Vladivostok"),
        Map.entry("якутск", "Yakutsk"),
        Map.entry("мурманск", "Murmansk"),
        Map.entry("калининград", "Kaliningrad"),
        Map.entry("архангельск", "Arkhangelsk"),
        Map.entry("тула", "Tula"),
        Map.entry("рязань", "Ryazan"),
        Map.entry("астрахань", "Astrakhan"),
        Map.entry("пенза", "Penza"),
        Map.entry("липецк", "Lipetsk"),
        Map.entry("кемерово", "Kemerovo"),
        Map.entry("томск", "Tomsk"),
        Map.entry("нальчик", "Nalchik"),
        Map.entry("грозный", "Grozny"),
        Map.entry("магнитогорск", "Magnitogorsk"),
        Map.entry("сочи", "Sochi"),
        Map.entry("ростов-на-дону", "Rostov-on-Don"),
        Map.entry("уфа", "Ufa"),
        Map.entry("воркута", "Vorkuta"),
        Map.entry("сыктывкар", "Syktyvkar"),
        Map.entry("петрозаводск", "Petrozavodsk"),
        Map.entry("майкоп", "Maykop"),
        Map.entry("элиста", "Elista"),
        Map.entry("черкесск", "Cherkessk"),
        Map.entry("назрань", "Nazran"),
        Map.entry("махачкала", "Makhachkala"),
        Map.entry("владикавказ", "Vladikavkaz"),
        Map.entry("саранск", "Saransk"),
        Map.entry("йошкар-ола", "Yoshkar-Ola"),
        Map.entry("чебоксары", "Cheboksary"),
        Map.entry("киров", "Kirov"),
        Map.entry("великий новгород", "Veliky Novgorod"),
        Map.entry("псков", "Pskov"),
        Map.entry("смоленск", "Smolensk"),
        Map.entry("брянск", "Bryansk"),
        Map.entry("орёл", "Oryol"),
        Map.entry("курск", "Kursk"),
        Map.entry("белгород", "Belgorod"),
        Map.entry("тамбов", "Tambov"),
        Map.entry("кострома", "Kostroma"),
        Map.entry("иваново", "Ivanovo"),
        Map.entry("владимир", "Vladimir"),
        Map.entry("ярославль", "Yaroslavl"),
        Map.entry("тверь", "Tver"),
        Map.entry("волжский", "Volzhsky"),
        Map.entry("находка", "Nakhodka"),
        Map.entry("ангарск", "Angarsk"),
        Map.entry("братск", "Bratsk"),
        Map.entry("арзамас", "Arzamas"),
        Map.entry("балашиха", "Balashikha"),
        Map.entry("химки", "Khimki"),
        Map.entry("мытищи", "Mytishchi"),
        Map.entry("королёв", "Korolyov"),
        Map.entry("одинцово", "Odintsovo"),
        Map.entry("коломна", "Kolomna"),
        Map.entry("серпухов", "Serpukhov"),
        Map.entry("новочеркасск", "Novocherkassk"),
        Map.entry("таганрог", "Taganrog"),
        Map.entry("новороссийск", "Novorossiysk"),
        Map.entry("южно-сахалинск", "Yuzhno-Sakhalinsk"),
        Map.entry("петропавловск-камчатский", "Petropavlovsk-Kamchatsky"),
        Map.entry("магадан", "Magadan"),
        Map.entry("алматы", "Almaty"),
        Map.entry("астана", "Astana"),
        Map.entry("шимкент", "Shymkent"),
        Map.entry("караганда", "Karaganda"),
        Map.entry("актобе", "Aktobe"),
        Map.entry("атрау", "Atyrau"),
        Map.entry("павлодар", "Pavlodar"),
        Map.entry("уст-каменогорск", "Ust-Kamenogorsk"),
        Map.entry("семей", "Semey"),
        Map.entry("костанай", "Kostanay"),
        Map.entry("кызылорда", "Kyzylorda"),
        Map.entry("актау", "Aktau"),
        Map.entry("тараз", "Taraz"),
        Map.entry("туркестан", "Turkistan"),
        Map.entry("балхаш", "Balkhash"),
        Map.entry("жезказган", "Jezkazgan"),
        Map.entry("кокшетау", "Kokshetau"),
        Map.entry("талдыкорган", "Taldykorgan"),
        Map.entry("уральск", "Oral"),
        Map.entry("петропавловск", "Petropavl"),
        Map.entry("киев", "Kyiv"),
        Map.entry("минск", "Minsk"),
        Map.entry("ташкент", "Tashkent"),
        Map.entry("бишкек", "Bishkek"),
        Map.entry("душанбе", "Dushanbe"),
        Map.entry("баку", "Baku"),
        Map.entry("ереван", "Yerevan"),
        Map.entry("тбилиси", "Tbilisi"),
        Map.entry("кишинёв", "Chisinau"),
        Map.entry("париж", "Paris"),
        Map.entry("берлин", "Berlin"),
        Map.entry("лондон", "London"),
        Map.entry("мадрид", "Madrid"),
        Map.entry("рим", "Rome"),
        Map.entry("амстердам", "Amsterdam"),
        Map.entry("вена", "Vienna"),
        Map.entry("стокгольм", "Stockholm"),
        Map.entry("хельсинки", "Helsinki"),
        Map.entry("осло", "Oslo"),
        Map.entry("копенгаген", "Copenhagen"),
        Map.entry("лиссабон", "Lisbon"),
        Map.entry("афины", "Athens"),
        Map.entry("стамбул", "Istanbul"),
        Map.entry("варшава", "Warsaw"),
        Map.entry("прага", "Prague"),
        Map.entry("рига", "Riga"),
        Map.entry("вильнюс", "Vilnius"),
        Map.entry("таллин", "Tallinn"),
        Map.entry("токио", "Tokyo"),
        Map.entry("осака", "Osaka"),
        Map.entry("пекин", "Beijing"),
        Map.entry("шанхай", "Shanghai"),
        Map.entry("сеул", "Seoul"),
        Map.entry("бангкок", "Bangkok"),
        Map.entry("дубай", "Dubai"),
        Map.entry("сингапур", "Singapore"),
        Map.entry("дели", "Delhi"),
        Map.entry("мумбаи", "Mumbai"),
        Map.entry("каир", "Cairo"),
        Map.entry("йоханнесбург", "Johannesburg"),
        Map.entry("сидней", "Sydney"),
        Map.entry("окленд", "Auckland")
    );

    private static final Map<String, String> COUNTRY_RU_TO_EN = Map.ofEntries(
        Map.entry("Россия", "Russia"),
        Map.entry("Казахстан", "Kazakhstan"),
        Map.entry("Украина", "Ukraine"),
        Map.entry("Беларусь", "Belarus"),
        Map.entry("Узбекистан", "Uzbekistan"),
        Map.entry("Кыргызстан", "Kyrgyzstan"),
        Map.entry("Таджикистан", "Tajikistan"),
        Map.entry("Азербайджан", "Azerbaijan"),
        Map.entry("Армения", "Armenia"),
        Map.entry("Грузия", "Georgia"),
        Map.entry("Молдова", "Moldova"),
        Map.entry("Франция", "France"),
        Map.entry("Германия", "Germany"),
        Map.entry("Великобритания", "United Kingdom"),
        Map.entry("Испания", "Spain"),
        Map.entry("Италия", "Italy"),
        Map.entry("Нидерланды", "Netherlands"),
        Map.entry("Австрия", "Austria"),
        Map.entry("Швеция", "Sweden"),
        Map.entry("Финляндия", "Finland"),
        Map.entry("Норвегия", "Norway"),
        Map.entry("Дания", "Denmark"),
        Map.entry("Португалия", "Portugal"),
        Map.entry("Греция", "Greece"),
        Map.entry("Турция", "Turkey"),
        Map.entry("Польша", "Poland"),
        Map.entry("Чехия", "Czech Republic"),
        Map.entry("Латвия", "Latvia"),
        Map.entry("Литва", "Lithuania"),
        Map.entry("Эстония", "Estonia"),
        Map.entry("Япония", "Japan"),
        Map.entry("Китай", "China"),
        Map.entry("Южная Корея", "South Korea"),
        Map.entry("Таиланд", "Thailand"),
        Map.entry("ОАЭ", "UAE"),
        Map.entry("Сингапур", "Singapore"),
        Map.entry("Индия", "India"),
        Map.entry("США", "USA"),
        Map.entry("Канада", "Canada"),
        Map.entry("Мексика", "Mexico"),
        Map.entry("Аргентина", "Argentina"),
        Map.entry("Бразилия", "Brazil"),
        Map.entry("Египет", "Egypt"),
        Map.entry("ЮАР", "South Africa"),
        Map.entry("Австралия", "Australia"),
        Map.entry("Новая Зеландия", "New Zealand")
    );

    private static final Map<String, String> CONTINENT_RU_TO_EN = Map.ofEntries(
        Map.entry("Европа", "Europe"),
        Map.entry("Азия", "Asia"),
        Map.entry("Африка", "Africa"),
        Map.entry("Северная Америка", "North America"),
        Map.entry("Южная Америка", "South America"),
        Map.entry("Океания", "Oceania"),
        Map.entry("Европа/Азия", "Europe/Asia"),
        Map.entry("Антарктида", "Antarctica")
    );

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }
      @PostConstruct
    public void initializeDefaultCities() {
        if (cityRepository.count() == 0) {
            log.info("📚 Инициализация базы городов...");
            
            // РОССИЯ (80 городов)
            save("москва", "Europe/Moscow", "Россия", "Европа");
            save("санкт-петербург", "Europe/Moscow", "Россия", "Европа");
            save("новосибирск", "Asia/Novosibirsk", "Россия", "Азия");
            save("екатеринбург", "Asia/Yekaterinburg", "Россия", "Азия");
            save("казань", "Europe/Moscow", "Россия", "Европа");
            save("нижний новгород", "Europe/Moscow", "Россия", "Европа");
            save("самара", "Europe/Samara", "Россия", "Европа");
            save("челябинск", "Asia/Yekaterinburg", "Россия", "Азия");
            save("омск", "Asia/Omsk", "Россия", "Азия");
            save("красноярск", "Asia/Krasnoyarsk", "Россия", "Азия");
            save("воронеж", "Europe/Moscow", "Россия", "Европа");
            save("пермь", "Asia/Yekaterinburg", "Россия", "Азия");
            save("волгоград", "Europe/Volgograd", "Россия", "Европа");
            save("краснодар", "Europe/Moscow", "Россия", "Европа");
            save("саратов", "Europe/Saratov", "Россия", "Европа");
            save("тюмень", "Asia/Yekaterinburg", "Россия", "Азия");
            save("тольятти", "Europe/Samara", "Россия", "Европа");
            save("ижевск", "Europe/Samara", "Россия", "Европа");
            save("барнаул", "Asia/Barnaul", "Россия", "Азия");
            save("иркутск", "Asia/Irkutsk", "Россия", "Азия");
            save("хабаровск", "Asia/Vladivostok", "Россия", "Азия");
            save("владивосток", "Asia/Vladivostok", "Россия", "Азия");
            save("якутск", "Asia/Yakutsk", "Россия", "Азия");
            save("мурманск", "Europe/Moscow", "Россия", "Европа");
            save("калининград", "Europe/Kaliningrad", "Россия", "Европа");
            save("архангельск", "Europe/Moscow", "Россия", "Европа");
            save("тула", "Europe/Moscow", "Россия", "Европа");
            save("рязань", "Europe/Moscow", "Россия", "Европа");
            save("астрахань", "Europe/Astrakhan", "Россия", "Европа");
            save("пенза", "Europe/Moscow", "Россия", "Европа");
            save("набережные челны", "Europe/Moscow", "Россия", "Европа");
            save("липецк", "Europe/Moscow", "Россия", "Европа");
            save("кемерово", "Asia/Novosibirsk", "Россия", "Азия");
            save("томск", "Asia/Tomsk", "Россия", "Азия");
            save("нальчик", "Europe/Moscow", "Россия", "Европа");
            save("грозный", "Europe/Moscow", "Россия", "Европа");
            save("магнитогорск", "Asia/Yekaterinburg", "Россия", "Азия");
            save("сочи", "Europe/Moscow", "Россия", "Европа");
            save("ростов-на-дону", "Europe/Moscow", "Россия", "Европа");
            save("уфа", "Asia/Yekaterinburg", "Россия", "Азия");
            save("воркута", "Europe/Moscow", "Россия", "Европа");
            save("сыктывкар", "Europe/Moscow", "Россия", "Европа");
            save("петрозаводск", "Europe/Moscow", "Россия", "Европа");
            save("майкоп", "Europe/Moscow", "Россия", "Европа");
            save("элиста", "Europe/Moscow", "Россия", "Европа");
            save("черкесск", "Europe/Moscow", "Россия", "Европа");
            save("назрань", "Europe/Moscow", "Россия", "Европа");
            save("махачкала", "Europe/Moscow", "Россия", "Европа");
            save("владикавказ", "Europe/Moscow", "Россия", "Европа");
            save("саранск", "Europe/Moscow", "Россия", "Европа");
            save("йошкар-ола", "Europe/Moscow", "Россия", "Европа");
            save("чебоксары", "Europe/Moscow", "Россия", "Европа");
            save("киров", "Europe/Moscow", "Россия", "Европа");
            save("великий новгород", "Europe/Moscow", "Россия", "Европа");
            save("псков", "Europe/Moscow", "Россия", "Европа");
            save("смоленск", "Europe/Moscow", "Россия", "Европа");
            save("брянск", "Europe/Moscow", "Россия", "Европа");
            save("орёл", "Europe/Moscow", "Россия", "Европа");
            save("курск", "Europe/Moscow", "Россия", "Европа");
            save("белгород", "Europe/Moscow", "Россия", "Европа");
            save("тамбов", "Europe/Moscow", "Россия", "Европа");
            save("кострома", "Europe/Moscow", "Россия", "Европа");
            save("иваново", "Europe/Moscow", "Россия", "Европа");
            save("владимир", "Europe/Moscow", "Россия", "Европа");
            save("ярославль", "Europe/Moscow", "Россия", "Европа");
            save("тверь", "Europe/Moscow", "Россия", "Европа");
            save("обнинск", "Europe/Moscow", "Россия", "Европа");
            save("новокузнецк", "Asia/Novokuznetsk", "Россия", "Азия");
            save("рыбинск", "Europe/Moscow", "Россия", "Европа");
            save("прокопьевск", "Asia/Novokuznetsk", "Россия", "Азия");
            save("бийск", "Asia/Barnaul", "Россия", "Азия");
            save("балаково", "Europe/Samara", "Россия", "Европа");
            save("северодвинск", "Europe/Moscow", "Россия", "Европа");
            save("уссурийск", "Asia/Vladivostok", "Россия", "Азия");
            save("электросталь", "Europe/Moscow", "Россия", "Европа");
            save("альметьевск", "Europe/Samara", "Россия", "Европа");
            save("салават", "Asia/Yekaterinburg", "Россия", "Азия");
            save("копейск", "Asia/Yekaterinburg", "Россия", "Азия");
            save("рубцовск", "Asia/Barnaul", "Россия", "Азия");
            save("березники", "Asia/Yekaterinburg", "Россия", "Азия");
            save("сызрань", "Europe/Samara", "Россия", "Европа");
            save("южно-сахалинск", "Asia/Sakhalin", "Россия", "Азия");
            save("петропавловск-камчатский", "Asia/Kamchatka", "Россия", "Азия");
            save("магадан", "Asia/Magadan", "Россия", "Азия");
            
            // КАЗАХСТАН (20 городов)
            save("алматы", "Asia/Almaty", "Казахстан", "Азия");
            save("астана", "Asia/Almaty", "Казахстан", "Азия");
            save("шимкент", "Asia/Almaty", "Казахстан", "Азия");
            save("караганда", "Asia/Almaty", "Казахстан", "Азия");
            save("актобе", "Asia/Aqtobe", "Казахстан", "Азия");
            save("атрау", "Asia/Atyrau", "Казахстан", "Азия");
            save("павлодар", "Asia/Almaty", "Казахстан", "Азия");
            save("уст-каменогорск", "Asia/Almaty", "Казахстан", "Азия");
            save("семей", "Asia/Almaty", "Казахстан", "Азия");
            save("костанай", "Asia/Qostanay", "Казахстан", "Азия");
            save("кызылорда", "Asia/Qyzylorda", "Казахстан", "Азия");
            save("актау", "Asia/Aqtau", "Казахстан", "Азия");
            save("тараз", "Asia/Almaty", "Казахстан", "Азия");
            save("туркестан", "Asia/Almaty", "Казахстан", "Азия");
            save("балхаш", "Asia/Almaty", "Казахстан", "Азия");
            save("жезказган", "Asia/Almaty", "Казахстан", "Азия");
            save("кокшетау", "Asia/Almaty", "Казахстан", "Азия");
            save("талдыкорган", "Asia/Almaty", "Казахстан", "Азия");
            save("уральск", "Asia/Oral", "Казахстан", "Азия");
            save("петропавловск", "Asia/Almaty", "Казахстан", "Азия");
            
            // СНГ (10 городов)
            save("киев", "Europe/Kyiv", "Украина", "Европа");
            save("минск", "Europe/Minsk", "Беларусь", "Европа");
            save("ташкент", "Asia/Tashkent", "Узбекистан", "Азия");
            save("бишкек", "Asia/Bishkek", "Кыргызстан", "Азия");
            save("душанбе", "Asia/Dushanbe", "Таджикистан", "Азия");
            save("баку", "Asia/Baku", "Азербайджан", "Азия");
            save("ереван", "Asia/Yerevan", "Армения", "Азия");
            save("тбилиси", "Asia/Tbilisi", "Грузия", "Азия");
            save("кишинёв", "Europe/Chisinau", "Молдова", "Европа");
            save("львов", "Europe/Kyiv", "Украина", "Европа");
            
            // ЕВРОПА (100 городов)
            save("париж", "Europe/Paris", "Франция", "Европа");
            save("берлин", "Europe/Berlin", "Германия", "Европа");
            save("лондон", "Europe/London", "Великобритания", "Европа");
            save("мадрид", "Europe/Madrid", "Испания", "Европа");
            save("рим", "Europe/Rome", "Италия", "Европа");
            save("амстердам", "Europe/Amsterdam", "Нидерланды", "Европа");
            save("вена", "Europe/Vienna", "Австрия", "Европа");
            save("стокгольм", "Europe/Stockholm", "Швеция", "Европа");
            save("хельсинки", "Europe/Helsinki", "Финляндия", "Европа");
            save("осло", "Europe/Oslo", "Норвегия", "Европа");
            save("копенгаген", "Europe/Copenhagen", "Дания", "Европа");
            save("лиссабон", "Europe/Lisbon", "Португалия", "Европа");
            save("афины", "Europe/Athens", "Греция", "Европа");
            save("стамбул", "Europe/Istanbul", "Турция", "Европа/Азия");
            save("варшава", "Europe/Warsaw", "Польша", "Европа");
            save("прага", "Europe/Prague", "Чехия", "Европа");
            save("рига", "Europe/Riga", "Латвия", "Европа");
            save("вильнюс", "Europe/Vilnius", "Литва", "Европа");
            save("таллин", "Europe/Tallinn", "Эстония", "Европа");
            save("будапешт", "Europe/Budapest", "Венгрия", "Европа");
            save("бухарест", "Europe/Bucharest", "Румыния", "Европа");
            save("софия", "Europe/Sofia", "Болгария", "Европа");
            save("белград", "Europe/Belgrade", "Сербия", "Европа");
            save("загреб", "Europe/Zagreb", "Хорватия", "Европа");
            save("люблиана", "Europe/Ljubljana", "Словения", "Европа");
            save("братислава", "Europe/Bratislava", "Словакия", "Европа");
            save("дублин", "Europe/Dublin", "Ирландия", "Европа");
            save("брюссель", "Europe/Brussels", "Бельгия", "Европа");
            save("люксембург", "Europe/Luxembourg", "Люксембург", "Европа");
            save("берн", "Europe/Zurich", "Швейцария", "Европа");
            save("рейкьявик", "Atlantic/Reykjavik", "Исландия", "Европа");
            save("гданьск", "Europe/Warsaw", "Польша", "Европа");
            save("краков", "Europe/Warsaw", "Польша", "Европа");
            save("вроцлав", "Europe/Warsaw", "Польша", "Европа");
            save("мюнхен", "Europe/Berlin", "Германия", "Европа");
            save("гамбург", "Europe/Berlin", "Германия", "Европа");
            save("франкфурт", "Europe/Berlin", "Германия", "Европа");
            save("кёльн", "Europe/Berlin", "Германия", "Европа");
            save("штутгарт", "Europe/Berlin", "Германия", "Европа");
            save("марсель", "Europe/Paris", "Франция", "Европа");
            save("лион", "Europe/Paris", "Франция", "Европа");
            save("тулуза", "Europe/Paris", "Франция", "Европа");
            save("ницца", "Europe/Paris", "Франция", "Европа");
            save("неаполь", "Europe/Rome", "Италия", "Европа");
            save("милан", "Europe/Rome", "Италия", "Европа");
            save("флоренция", "Europe/Rome", "Италия", "Европа");
            save("венеция", "Europe/Rome", "Италия", "Европа");
            save("барселона", "Europe/Madrid", "Испания", "Европа");
            save("валенсия", "Europe/Madrid", "Испания", "Европа");
            save("севилья", "Europe/Madrid", "Испания", "Европа");
            save("манчестер", "Europe/London", "Великобритания", "Европа");
            save("ливерпуль", "Europe/London", "Великобритания", "Европа");
            save("эдинбург", "Europe/London", "Великобритания", "Европа");
            save("анкара", "Europe/Istanbul", "Турция", "Европа/Азия");
            save("измир", "Europe/Istanbul", "Турция", "Европа/Азия");
            save("анталья", "Europe/Istanbul", "Турция", "Европа/Азия");
            save("бурса", "Europe/Istanbul", "Турция", "Европа/Азия");
            save("адана", "Europe/Istanbul", "Турция", "Европа/Азия");
            save("роттердам", "Europe/Amsterdam", "Нидерланды", "Европа");
            save("гага", "Europe/Amsterdam", "Нидерланды", "Европа");
            save("утрехт", "Europe/Amsterdam", "Нидерланды", "Европа");
            save("антверпен", "Europe/Brussels", "Бельгия", "Европа");
            save("гент", "Europe/Brussels", "Бельгия", "Европа");
            save("цюрих", "Europe/Zurich", "Швейцария", "Европа");
            save("женева", "Europe/Zurich", "Швейцария", "Европа");
            save("базель", "Europe/Zurich", "Швейцария", "Европа");
            save("осло", "Europe/Oslo", "Норвегия", "Европа");
            save("берген", "Europe/Oslo", "Норвегия", "Европа");
            save("гётеборг", "Europe/Stockholm", "Швеция", "Европа");
            save("мальмё", "Europe/Stockholm", "Швеция", "Европа");
            save("тампере", "Europe/Helsinki", "Финляндия", "Европа");
            save("турку", "Europe/Helsinki", "Финляндия", "Европа");
            save("аархус", "Europe/Copenhagen", "Дания", "Европа");
            save("оденсе", "Europe/Copenhagen", "Дания", "Европа");
            save("порто", "Europe/Lisbon", "Португалия", "Европа");
            save("брага", "Europe/Lisbon", "Португалия", "Европа");
            save("салоники", "Europe/Athens", "Греция", "Европа");
            save("ираклион", "Europe/Athens", "Греция", "Европа");
            save("палермо", "Europe/Rome", "Италия", "Европа");
            save("турин", "Europe/Rome", "Италия", "Европа");
            save("генуя", "Europe/Rome", "Италия", "Европа");
            save("болонья", "Europe/Rome", "Италия", "Европа");
            save("катания", "Europe/Rome", "Италия", "Европа");
            save("сараево", "Europe/Sarajevo", "Босния и Герцеговина", "Европа");
            save("подгорица", "Europe/Podgorica", "Черногория", "Европа");
            save("приштина", "Europe/Belgrade", "Косово", "Европа");
            save("тирана", "Europe/Tirane", "Албания", "Европа");
            save("скопье", "Europe/Skopje", "Северная Македония", "Европа");
            save("никосия", "Asia/Nicosia", "Кипр", "Европа");
            save("мальта", "Europe/Malta", "Мальта", "Европа");
            save("монако", "Europe/Monaco", "Монако", "Европа");
            save("андорра-ла-велья", "Europe/Andorra", "Андорра", "Европа");
            save("вадуц", "Europe/Zurich", "Лихтенштейн", "Европа");
            save("сан-марино", "Europe/Rome", "Сан-Марино", "Европа");
            save("ватикан", "Europe/Rome", "Ватикан", "Европа");
            
            // АЗИЯ (80 городов)
            save("токио", "Asia/Tokyo", "Япония", "Азия");
            save("осака", "Asia/Tokyo", "Япония", "Азия");
            save("пекин", "Asia/Shanghai", "Китай", "Азия");
            save("шанхай", "Asia/Shanghai", "Китай", "Азия");
            save("сеул", "Asia/Seoul", "Южная Корея", "Азия");
            save("бангкок", "Asia/Bangkok", "Таиланд", "Азия");
            save("дубай", "Asia/Dubai", "ОАЭ", "Азия");
            save("сингапур", "Asia/Singapore", "Сингапур", "Азия");
            save("дели", "Asia/Kolkata", "Индия", "Азия");
            save("мумбаи", "Asia/Kolkata", "Индия", "Азия");
            save("шэньчжэнь", "Asia/Shanghai", "Китай", "Азия");
            save("гуанчжоу", "Asia/Shanghai", "Китай", "Азия");
            save("тяньцзинь", "Asia/Shanghai", "Китай", "Азия");
            save("ухань", "Asia/Shanghai", "Китай", "Азия");
            save("ханчжоу", "Asia/Shanghai", "Китай", "Азия");
            save("нанкин", "Asia/Shanghai", "Китай", "Азия");
            save("чэнду", "Asia/Shanghai", "Китай", "Азия");
            save("сиань", "Asia/Shanghai", "Китай", "Азия");
            save("чунцин", "Asia/Shanghai", "Китай", "Азия");
            save("харбин", "Asia/Shanghai", "Китай", "Азия");
            save("далянь", "Asia/Shanghai", "Китай", "Азия");
            save("циндао", "Asia/Shanghai", "Китай", "Азия");
            save("сямэнь", "Asia/Shanghai", "Китай", "Азия");
            save("нинбо", "Asia/Shanghai", "Китай", "Азия");
            save("фучжоу", "Asia/Shanghai", "Китай", "Азия");
            save("пхеньян", "Asia/Pyongyang", "КНДР", "Азия");
            save("хамхын", "Asia/Pyongyang", "КНДР", "Азия");
            save("чхонджин", "Asia/Pyongyang", "КНДР", "Азия");
            save("пусан", "Asia/Seoul", "Южная Корея", "Азия");
            save("инчхон", "Asia/Seoul", "Южная Корея", "Азия");
            save("тэгу", "Asia/Seoul", "Южная Корея", "Азия");
            save("тэджон", "Asia/Seoul", "Южная Корея", "Азия");
            save("кванджу", "Asia/Seoul", "Южная Корея", "Азия");
            save("ульсан", "Asia/Seoul", "Южная Корея", "Азия");
            save("пхукет", "Asia/Bangkok", "Таиланд", "Азия");
            save("чиангмай", "Asia/Bangkok", "Таиланд", "Азия");
            save("паттайя", "Asia/Bangkok", "Таиланд", "Азия");
            save("хатъяй", "Asia/Bangkok", "Таиланд", "Азия");
            save("кхонкэн", "Asia/Bangkok", "Таиланд", "Азия");
            save("удонтхани", "Asia/Bangkok", "Таиланд", "Азия");
            save("дананг", "Asia/Ho_Chi_Minh", "Вьетнам", "Азия");
            save("хошимин", "Asia/Ho_Chi_Minh", "Вьетнам", "Азия");
            save("хайфон", "Asia/Ho_Chi_Minh", "Вьетнам", "Азия");
            save("ханой", "Asia/Ho_Chi_Minh", "Вьетнам", "Азия");
            save("куала-лумпур", "Asia/Kuala_Lumpur", "Малайзия", "Азия");
            save("джохор-бару", "Asia/Kuala_Lumpur", "Малайзия", "Азия");
            save("пенанг", "Asia/Kuala_Lumpur", "Малайзия", "Азия");
            save("кучинг", "Asia/Kuching", "Малайзия", "Азия");
            save("иппо", "Asia/Kuala_Lumpur", "Малайзия", "Азия");
            save("манила", "Asia/Manila", "Филиппины", "Азия");
            save("кезон-сити", "Asia/Manila", "Филиппины", "Азия");
            save("давао", "Asia/Manila", "Филиппины", "Азия");
            save("калоокан", "Asia/Manila", "Филиппины", "Азия");
            save("себу", "Asia/Manila", "Филиппины", "Азия");
            save("замбоанга", "Asia/Manila", "Филиппины", "Азия");
            save("бенгалуру", "Asia/Kolkata", "Индия", "Азия");
            save("ченнай", "Asia/Kolkata", "Индия", "Азия");
            save("хайдерабад", "Asia/Kolkata", "Индия", "Азия");
            save("ахмадабад", "Asia/Kolkata", "Индия", "Азия");
            save("пуне", "Asia/Kolkata", "Индия", "Азия");
            save("калькутта", "Asia/Kolkata", "Индия", "Азия");
            save("сурат", "Asia/Kolkata", "Индия", "Азия");
            save("джайпур", "Asia/Kolkata", "Индия", "Азия");
            save("лакхнау", "Asia/Kolkata", "Индия", "Азия");
            save("канпур", "Asia/Kolkata", "Индия", "Азия");
            save("нагпур", "Asia/Kolkata", "Индия", "Азия");
            save("индор", "Asia/Kolkata", "Индия", "Азия");
            save("бхопал", "Asia/Kolkata", "Индия", "Азия");
            save("патна", "Asia/Kolkata", "Индия", "Азия");
            save("карачи", "Asia/Karachi", "Пакистан", "Азия");
            save("лахор", "Asia/Karachi", "Пакистан", "Азия");
            save("исламабад", "Asia/Karachi", "Пакистан", "Азия");
            save("файсалабад", "Asia/Karachi", "Пакистан", "Азия");
            save("равалпинди", "Asia/Karachi", "Пакистан", "Азия");
            save("мултан", "Asia/Karachi", "Пакистан", "Азия");
            save("кабул", "Asia/Kabul", "Афганистан", "Азия");
            save("кандагар", "Asia/Kabul", "Афганистан", "Азия");
            save("герат", "Asia/Kabul", "Афганистан", "Азия");
            save("мазари-шариф", "Asia/Kabul", "Афганистан", "Азия");
            
            // АМЕРИКА (70 городов)
            save("нью-йорк", "America/New_York", "США", "Северная Америка");
            save("лос-анджелес", "America/Los_Angeles", "США", "Северная Америка");
            save("чикаго", "America/Chicago", "США", "Северная Америка");
            save("торонто", "America/Toronto", "Канада", "Северная Америка");
            save("мехико", "America/Mexico_City", "Мексика", "Северная Америка");
            save("буэнос-айрес", "America/Argentina/Buenos_Aires", "Аргентина", "Южная Америка");
            save("сан-паулу", "America/Sao_Paulo", "Бразилия", "Южная Америка");
            save("лима", "America/Lima", "Перу", "Южная Америка");
            save("богота", "America/Bogota", "Колумбия", "Южная Америка");
            save("сантьяго", "America/Santiago", "Чили", "Южная Америка");
            save("каракас", "America/Caracas", "Венесуэла", "Южная Америка");
            save("кито", "America/Guayaquil", "Эквадор", "Южная Америка");
            save("гавана", "America/Havana", "Куба", "Северная Америка");
            save("сан-хосе", "America/Costa_Rica", "Коста-Рика", "Северная Америка");
            save("панама", "America/Panama", "Панама", "Северная Америка");
            save("гватемала", "America/Guatemala", "Гватемала", "Северная Америка");
            save("вашингтон", "America/New_York", "США", "Северная Америка");
            save("бостон", "America/New_York", "США", "Северная Америка");
            save("майами", "America/New_York", "США", "Северная Америка");
            save("атланта", "America/New_York", "США", "Северная Америка");
            save("даллас", "America/Chicago", "США", "Северная Америка");
            save("хьюстон", "America/Chicago", "США", "Северная Америка");
            save("филадельфия", "America/New_York", "США", "Северная Америка");
            save("феникс", "America/Phoenix", "США", "Северная Америка");
            save("сан-антонио", "America/Chicago", "США", "Северная Америка");
            save("сан-диего", "America/Los_Angeles", "США", "Северная Америка");
            save("денвер", "America/Denver", "США", "Северная Америка");
            save("сан-франциско", "America/Los_Angeles", "США", "Северная Америка");
            save("сиэтл", "America/Los_Angeles", "США", "Северная Америка");
            save("миннеаполис", "America/Chicago", "США", "Северная Америка");
            save("тампа", "America/New_York", "США", "Северная Америка");
            save("питтсбург", "America/New_York", "США", "Северная Америка");
            save("сент-луис", "America/Chicago", "США", "Северная Америка");
            save("сан-хуан", "America/Puerto_Rico", "Пуэрто-Рико", "Северная Америка");
            save("ванкувер", "America/Vancouver", "Канада", "Северная Америка");
            save("монреаль", "America/Toronto", "Канада", "Северная Америка");
            save("калгари", "America/Edmonton", "Канада", "Северная Америка");
            save("оттава", "America/Toronto", "Канада", "Северная Америка");
            save("эдмонтон", "America/Edmonton", "Канада", "Северная Америка");
            save("гвадалахара", "America/Mexico_City", "Мексика", "Северная Америка");
            save("монтеррей", "America/Mexico_City", "Мексика", "Северная Америка");
            save("канкун", "America/Cancun", "Мексика", "Северная Америка");
            save("тихуана", "America/Tijuana", "Мексика", "Северная Америка");
            save("леон", "America/Mexico_City", "Мексика", "Северная Америка");
            save("пуэбла", "America/Mexico_City", "Мексика", "Северная Америка");
            save("рио-де-жанейро", "America/Sao_Paulo", "Бразилия", "Южная Америка");
            save("бразилиа", "America/Sao_Paulo", "Бразилия", "Южная Америка");
            save("салвадор", "America/Bahia", "Бразилия", "Южная Америка");
            save("форталеза", "America/Fortaleza", "Бразилия", "Южная Америка");
            save("белу-оризонти", "America/Sao_Paulo", "Бразилия", "Южная Америка");
            save("манаус", "America/Manaus", "Бразилия", "Южная Америка");
            save("куритаба", "America/Sao_Paulo", "Бразилия", "Южная Америка");
            save("ресифи", "America/Recife", "Бразилия", "Южная Америка");
            save("порталегри", "America/Sao_Paulo", "Бразилия", "Южная Америка");
            save("монтевидео", "America/Montevideo", "Уругвай", "Южная Америка");
            save("кордова", "America/Argentina/Cordoba", "Аргентина", "Южная Америка");
            save("росарио", "America/Argentina/Cordoba", "Аргентина", "Южная Америка");
            save("мендоса", "America/Argentina/Mendoza", "Аргентина", "Южная Америка");
            save("ла-пас", "America/La_Paz", "Боливия", "Южная Америка");
            save("санта-крус", "America/La_Paz", "Боливия", "Южная Америка");
            save("асунсьон", "America/Asuncion", "Парагвай", "Южная Америка");
            save("медельин", "America/Bogota", "Колумбия", "Южная Америка");
            save("кали", "America/Bogota", "Колумбия", "Южная Америка");
            save("барранкилья", "America/Bogota", "Колумбия", "Южная Америка");
            save("картахена", "America/Bogota", "Колумбия", "Южная Америка");
            save("гуаякиль", "America/Guayaquil", "Эквадор", "Южная Америка");
            save("куэнко", "America/Guayaquil", "Эквадор", "Южная Америка");
            save("вала́парайсо", "America/Santiago", "Чили", "Южная Америка");
            save("консепсьон", "America/Santiago", "Чили", "Южная Америка");
            save("антофагаста", "America/Santiago", "Чили", "Южная Америка");
            
            // АФРИКА (40 городов)
            save("каир", "Africa/Cairo", "Египет", "Африка");
            save("йоханнесбург", "Africa/Johannesburg", "ЮАР", "Африка");
            save("лагос", "Africa/Lagos", "Нигерия", "Африка");
            save("найроби", "Africa/Nairobi", "Кения", "Африка");
            save("касабланка", "Africa/Casablanca", "Марокко", "Африка");
            save("кейптаун", "Africa/Johannesburg", "ЮАР", "Африка");
            save("дурбан", "Africa/Johannesburg", "ЮАР", "Африка");
            save("аддис-абеба", "Africa/Addis_Ababa", "Эфиопия", "Африка");
            save("дар-эс-салам", "Africa/Dar_es_Salaam", "Танзания", "Африка");
            save("камкала", "Africa/Kampala", "Уганда", "Африка");
            save("аккра", "Africa/Accra", "Гана", "Африка");
            save("абуджа", "Africa/Lagos", "Нигерия", "Африка");
            save("тунис", "Africa/Tunis", "Тунис", "Африка");
            save("алжир", "Africa/Algiers", "Алжир", "Африка");
            save("рабат", "Africa/Casablanca", "Марокко", "Африка");
            save("дакар", "Africa/Dakar", "Сенегал", "Африка");
            save("бамако", "Africa/Bamako", "Мали", "Африка");
            save("уагадугу", "Africa/Ouagadougou", "Буркина-Фасо", "Африка");
            save("ниамей", "Africa/Niamey", "Нигер", "Африка");
            save("нджамена", "Africa/Ndjamena", "Чад", "Африка");
            save("хартум", "Africa/Khartoum", "Судан", "Африка");
            save("могадишо", "Africa/Mogadishu", "Сомали", "Африка");
            save("луанда", "Africa/Luanda", "Ангола", "Африка");
            save("хараре", "Africa/Harare", "Зимбабве", "Африка");
            save("лусака", "Africa/Lusaka", "Замбия", "Африка");
            save("мапуту", "Africa/Maputo", "Мозамбик", "Африка");
            save("претория", "Africa/Johannesburg", "ЮАР", "Африка");
            save("киншаса", "Africa/Kinshasa", "ДР Конго", "Африка");
            save("кано", "Africa/Lagos", "Нигерия", "Африка");
            save("ибадан", "Africa/Lagos", "Нигерия", "Африка");
            save("фритаун", "Africa/Freetown", "Сьерра-Леоне", "Африка");
            save("монровия", "Africa/Monrovia", "Либерия", "Африка");
            save("абиджан", "Africa/Abidjan", "Кот-д'Ивуар", "Африка");
            save("яунде", "Africa/Douala", "Камерун", "Африка");
            save("дуа́ла", "Africa/Douala", "Камерун", "Африка");
            save("либревиль", "Africa/Libreville", "Габон", "Африка");
            save("антананариву", "Indian/Antananarivo", "Мадагаскар", "Африка");
            save("порт-луи", "Indian/Mauritius", "Маврикий", "Африка");
            save("виктория", "Indian/Mahe", "Сейшельские острова", "Африка");
            
            // ОКЕАНИЯ (40 городов)
            save("сидней", "Australia/Sydney", "Австралия", "Океания");
            save("окленд", "Pacific/Auckland", "Новая Зеландия", "Океания");
            save("мельбурн", "Australia/Melbourne", "Австралия", "Океания");
            save("брисбен", "Australia/Brisbane", "Австралия", "Океания");
            save("перт", "Australia/Perth", "Австралия", "Океания");
            save("аделаида", "Australia/Adelaide", "Австралия", "Океания");
            save("веллингтон", "Pacific/Auckland", "Новая Зеландия", "Океания");
            save("крайстчерч", "Pacific/Auckland", "Новая Зеландия", "Океания");
            save("хобарт", "Australia/Hobart", "Австралия", "Океания");
            save("дарвин", "Australia/Darwin", "Австралия", "Океания");
            save("сува", "Pacific/Fiji", "Фиджи", "Океания");
            save("порт-морсби", "Pacific/Port_Moresby", "Папуа-Новая Гвинея", "Океания");
            save("голд-кост", "Australia/Brisbane", "Австралия", "Океания");
            save("канберра", "Australia/Sydney", "Австралия", "Океания");
            save("ньюкасл", "Australia/Sydney", "Австралия", "Океания");
            save("вулонгонг", "Australia/Sydney", "Австралия", "Океания");
            save("геелонг", "Australia/Melbourne", "Австралия", "Океания");
            save("таунсвилл", "Australia/Brisbane", "Австралия", "Океания");
            save("кэрнс", "Australia/Brisbane", "Австралия", "Океания");
            save("алис-спрингс", "Australia/Darwin", "Австралия", "Океания");
            save("брум", "Australia/Perth", "Австралия", "Океания");
            save("порт-хедленд", "Australia/Perth", "Австралия", "Океания");
            save("калгурли", "Australia/Perth", "Австралия", "Океания");
            save("бунд", "Australia/Perth", "Австралия", "Океания");
            save("олбани", "Australia/Perth", "Австралия", "Океания");
            save("лонсестон", "Australia/Hobart", "Австралия", "Океания");
            save("девенпорт", "Australia/Hobart", "Австралия", "Океания");
            save("гамильтон", "Pacific/Auckland", "Новая Зеландия", "Океания");
            save("тауранга", "Pacific/Auckland", "Новая Зеландия", "Океания");
            save("нейпир", "Pacific/Auckland", "Новая Зеландия", "Океания");
            save("палмерстон-норт", "Pacific/Auckland", "Новая Зеландия", "Океания");
            save("данедин", "Pacific/Auckland", "Новая Зеландия", "Океания");
            save("инверкаргилл", "Pacific/Auckland", "Новая Зеландия", "Океания");
            save("нумеа", "Pacific/Noumea", "Новая Каледония", "Океания");
            save("папеэте", "Pacific/Tahiti", "Французская Полинезия", "Океания");
            save("апиа", "Pacific/Apia", "Самоа", "Океания");
            save("нукуалофа", "Pacific/Tongatapu", "Тонга", "Океания");
            save("тарава", "Pacific/Tarawa", "Кирибати", "Океания");
            save("маджуро", "Pacific/Majuro", "Маршалловы острова", "Океания");
            save("паликир", "Pacific/Chuuk", "Микронезия", "Океания");
            
            log.info("✅ База городов инициализирована. Всего городов: {}", cityRepository.count());
        } else {
            log.info("📚 База городов уже существует. Городов: {}", cityRepository.count());
        }
    }
    
    private void save(String name, String tz, String country, String continent) {
        cityRepository.save(new City(name.toLowerCase(), tz, country, continent));
    }
      

    public City findCity(String name) {
        if (name == null) return null;
        String query = name.toLowerCase().trim();
        String ruName = EN_TO_RU.get(query);
        if (ruName != null) query = ruName;
        return cityRepository.findById(query).orElse(null);
    }

    public List<City> searchCities(String query) {
        if (query == null || query.length() < 2) return List.of();
        return cityRepository.findByNameContainingIgnoreCase(query.trim());
    }

    public String getCityNameLocalized(City city, String lang) {
        if ("en".equals(lang)) {
            return RU_TO_EN.getOrDefault(city.getName(), capitalizeFirst(city.getName()));
        }
        return capitalizeFirst(city.getName());
    }

    public String getCountryLocalized(City city, String lang) {
        if ("en".equals(lang)) {
            return COUNTRY_RU_TO_EN.getOrDefault(city.getCountry(), city.getCountry());
        }
        return city.getCountry();
    }
    
    public String getContinentLocalized(City city, String lang) {
        if ("en".equals(lang)) {
            return CONTINENT_RU_TO_EN.getOrDefault(city.getContinent(), city.getContinent());
        }
        return city.getContinent();
    }
    public String getCityInfo(City city, String lang) {
        return getCityInfo(city, lang, "24");
    }
    
    public String getCityInfo(City city, String lang, String timeFormat) {
        ZoneId zone = ZoneId.of(city.getTimezone());
        ZonedDateTime now = ZonedDateTime.now(zone);
        Locale locale = "en".equals(lang) ? Locale.ENGLISH : new Locale("ru");
        String datePattern = "en".equals(lang) ? "dd MMMM yyyy, EEEE" : "dd MMMM yyyy, EEEE";
        String dateStr = capitalizeFirst(now.format(DateTimeFormatter.ofPattern(datePattern, locale)));
        
        // Определяем формат времени
        String timePattern = "12".equals(timeFormat) ? "hh:mm a" : "HH:mm";
        String timeStr = now.format(DateTimeFormatter.ofPattern(timePattern, "12".equals(timeFormat) && "en".equals(lang) ? Locale.ENGLISH : locale));
        
        int offsetSeconds = now.getOffset().getTotalSeconds();
        int offsetHours = offsetSeconds / 3600;
        int offsetMinutes = Math.abs((offsetSeconds % 3600) / 60);
        String diffStr = String.format("%s%d:%02d", offsetHours >= 0 ? "+" : "", offsetHours, offsetMinutes);
        String diffLabel = "en".equals(lang) ? "Time diff" : "Разница с UTC";
        String cityName = getCityNameLocalized(city, lang);
        String countryName = getCountryLocalized(city, lang);
        String continentName = getContinentLocalized(city, lang);

        return "🌍 <b>" + cityName + "</b>\n" +
               "📍 " + countryName + ", " + continentName + "\n" +
               "🕐 <b>" + timeStr + "</b> | 📅 " + dateStr + "\n" +
               "🌐 " + city.getTimezone() + " | ⏱️ " + diffLabel + ": " + diffStr;
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
