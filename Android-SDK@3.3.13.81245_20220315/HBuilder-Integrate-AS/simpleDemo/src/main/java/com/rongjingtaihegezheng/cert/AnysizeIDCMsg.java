package com.rongjingtaihegezheng.cert;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

public class AnysizeIDCMsg {
	public final static byte[] byLicData = {(byte)0x05,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x5B,(byte)0x03,(byte)0x33,(byte)0x01,(byte)0x5A,(byte)0xB3,(byte)0x1E,(byte)0x00};
	
	public final static int NAME_INDEX 		= 0;	//中文名
	public final static int SEX_INDEX		= 1;	//性别
	public final static int NATION_INDEX	= 2;	//民族
	public final static int BIRTH_INDEX		= 3;	//出生日期
	public final static int ADDR_INDEX		= 4;	//地址
	public final static int IDNUM_INDEX		= 5;	//身份证号
	public final static int DEPART_INDEX	= 6;	//签发机关
	public final static int STATIME_INDEX	= 7;	//有效日期
	public final static int ENDTIME_INDEX	= 8;	//截止日期
	/*外国人多出的数据*/
	public final static int FGRNAME_INDEX	= 9;	//英文名
	public final static int COUNTRY_INDEX	= 10;	//国家或所在地区代码
	public final static int VERSION_INDEX	= 11;	//证件版本
	public final static int IDTYPE_INDEX	= 12;	//证件类型
	public final static int SLJGCODE_INDEX	= 13;	//当次受理机关代码
	/*港澳台多出的数据(包括证件类型)*/
	public final static int QFCS_INDEX		= 14;	//签发次数
	public final static int TXZHM_INDEX		= 15;	//通行证号码
	public final static int CARDTYPE_INDEX	= 16;	//CHN:中国大陆身份证、GAT:港澳台居民证、FGR：外国人
	
	//外国人国家三字节代码
	private static String Country[][] = {
		{"AFG","阿富汗"},
		{"ALB","阿尔巴尼亚"},
		{"DZA","阿尔及利亚"},
		{"ASM","美属萨摩亚"},
		{"AND","安道尔"},
		{"AGO","安哥拉"},
		{"AIA","安圭拉"},
		{"ATA","南极洲"},
		{"ATG","安提瓜和巴布达"},
		{"ARG","阿根廷"},
		{"ARM","亚美尼亚"},
		{"ABW","阿鲁巴"},
		{"AUS","澳大利亚"},
		{"AUT","奥地利"},
		{"AZE","阿塞拜疆"},
		{"BHS","巴哈马"},
		{"BHR","巴林"},
		{"BGD","孟加拉国"},
		{"BRB","巴巴多斯"},
		{"BLR","白俄罗斯"},
		{"BEL","比利时"},
		{"BLZ","伯利兹"},
		{"BEN","贝宁"},
		{"BMU","百慕大"},
		{"BTN","不丹"},
		{"BOL","玻利维亚"},
		{"BIH","波黑"},
		{"BWA","博茨瓦纳"},
		{"BVT","布维岛"},
		{"BRA","巴西"},
		{"IOT","英属印度洋领土"},
		{"BRN","文莱"},
		{"BGR","保加利亚"},
		{"BFA","布基纳法索"},
		{"BDI","布隆迪"},
		{"KHM","柬埔寨"},
		{"CMR","喀麦隆"},
		{"CAN","加拿大"},
		{"CPV","佛得角"},
		{"CYM","开曼群岛"},
		{"CAF","中非"},
		{"TCD","乍得"},
		{"CHL","智利"},
		{"CHN","中国"},
		{"HKG","香港"},
		{"MAC","澳门"},
		{"TWN","台湾"},
		{"CSR","圣诞岛"},
		{"CCK","科科斯(基林)群岛"},
		{"COL","哥伦比亚"},
		{"COM","科摩罗"},
		{"COG","刚果（布）"},
		{"COD","刚果（金）"},
		{"COK","库克群岛"},
		{"CR","斯达黎加"},
		{"CIV","科特迪瓦"},
		{"HRV","克罗地亚"},
		{"CUB","古巴"},
		{"CYP","塞浦路斯"},
		{"CZE","捷克"},
		{"DNK","丹麦"},
		{"DJI","吉布提"},
		{"DMA","多米尼克"},
		{"DOM","多米尼加共和国"},
		{"TMP","东帝汶"},
		{"ECU","厄瓜多尔"},
		{"EGY","埃及"},
		{"SLV","萨尔瓦多"},
		{"GNQ","赤道几内亚"},
		{"ERI","厄立特里亚"},
		{"EST","爱沙尼亚"},
		{"ETH","埃塞俄比亚"},
		{"FLK","福克兰群岛(马尔维纳斯)"},
		{"FRO","法罗群岛"},
		{"FJI","斐济"},
		{"FIN","芬兰"},
		{"FRA","法国"},
		{"GUF","法属圭亚那"},
		{"PYF","法属波利尼西亚"},
		{"ATF","法属南部领土"},
		{"GAB","加蓬"},
		{"GMB","冈比亚Gambia"},
		{"GEO","格鲁吉亚"},
		{"DEU","德国"},
		{"GHA","加纳"},
		{"GIB","直布罗陀"},
		{"GRC","希腊"},
		{"GRL","格陵兰"},
		{"GRD","格林纳达"},
		{"GLP","瓜德罗普"},
		{"GUM","关岛"},
		{"GTM","危地马拉"},
		{"GIN","几内亚"},
		{"GNB","几内亚比绍"},
		{"GUY","圭亚那"},
		{"HTI","海地"},
		{"HMD","赫德岛和麦克唐纳岛"},
		{"HND","洪都拉斯"},
		{"HUN","匈牙利"},
		{"ISL","冰岛"},
		{"IND","印度"},
		{"IDN","印度尼西亚"},
		{"IRN","伊朗"},
		{"IRQ","伊拉克"},
		{"IRL","爱尔兰"},
		{"ISR","以色列"},
		{"ITA","意大利"},
		{"JAM","牙买加"},
		{"JPN","日本"},
		{"JOR","约旦"},
		{"KAZ","哈萨克斯坦"},
		{"KEN","肯尼亚"},
		{"KIR","基里巴斯"},
		{"PRK","朝鲜"},
		{"KOR","韩国"},
		{"KWT","科威特"},
		{"KGZ","吉尔吉斯斯坦"},
		{"LAO","老挝"},
		{"LVA","拉脱维亚"},
		{"LBN","黎巴嫩"},
		{"LSO","莱索托"},
		{"LBR","利比里亚"},
		{"LBY","利比亚"},
		{"LIE","列支敦士登"},
		{"LTU","立陶宛"},
		{"LUX","卢森堡"},
		{"MKD","前南马其顿"},
		{"MDG","马达加斯加"},
		{"MWI","马拉维"},
		{"MYS","马来西亚"},
		{"MDV","马尔代夫"},
		{"MLI","马里"},
		{"MLT","马耳他"},
		{"MHL","马绍尔群岛"},
		{"MTQ","马提尼克"},
		{"MRT","毛里塔尼亚"},
		{"MUS","毛里求斯"},
		{"MYT","马约特"},
		{"MEX","墨西哥"},
		{"FSM","密克罗尼西亚联邦"},
		{"MDA","摩尔多瓦"},
		{"MCO","摩纳哥"},
		{"MNG","蒙古"},
		{"MSR","蒙特塞拉特"},
		{"MAR","摩洛哥"},
		{"MOZ","莫桑比克"},
		{"MMR","缅甸"},
		{"NAM","纳米比亚"},
		{"NRU","瑙鲁"},
		{"NPL","尼泊尔"},
		{"NLD","荷兰"},
		{"ANT","荷属安的列斯"},
		{"NCL","新喀里多尼亚"},
		{"NZL","新西兰"},
		{"NIC","尼加拉瓜"},
		{"NER","尼日尔"},
		{"NGA","尼日利亚"},
		{"NIU","纽埃"},
		{"NFK","诺福克岛"},
		{"MNP","北马里亚纳"},
		{"NOR","挪威"},
		{"OMN","阿曼"},
		{"PAK","巴基斯坦"},
		{"PLW","帕劳"},
		{"PST","巴勒斯坦"},
		{"PAN","巴拿马"},
		{"PNG","巴布亚新几内亚"},
		{"PRY","巴拉圭"},
		{"PER","秘鲁"},
		{"PHL","菲律宾"},
		{"PCN","皮特凯恩群岛"},
		{"POL","波兰"},
		{"PRT","葡萄牙"},
		{"PRI","波多黎各"},
		{"QAT","卡塔尔"},
		{"REU","留尼汪"},
		{"ROM","罗马尼亚"},
		{"RUS","俄罗斯联邦"},
		{"RWA","卢旺达"},
		{"SHN","圣赫勒拿"},
		{"KNA","圣基茨和尼维斯"},
		{"LCA","圣卢西亚"},
		{"SPM","圣皮埃尔和密克隆"},
		{"VCT","圣文森特和格林纳丁斯"},
		{"WSM","萨摩亚"},
		{"SMR","圣马力诺"},
		{"STp","圣多美和普林西比"},
		{"SAU","沙特阿拉伯"},
		{"SEN","塞内加尔"},
		{"SYC","塞舌尔"},
		{"SLE","塞拉利昂"},
		{"SGP","新加坡"},
		{"SVK","斯洛伐克"},
		{"SVN","斯洛文尼亚"},
		{"SLB","所罗门群岛"},
		{"SOM","索马里"},
		{"ZAF","南非"},
		{"SGS","南乔治亚岛和南桑德韦奇岛"},
		{"ESP","西班牙"},
		{"LKA","斯里兰卡"},
		{"SDN","苏丹"},
		{"SUR","苏里南"},
		{"SJM","斯瓦尔巴群岛"},
		{"SWZ","斯威士兰"},
		{"SWE","瑞典"},
		{"CHE","瑞士"},
		{"SYR","叙利亚"},
		{"TJK","塔吉克斯坦"},
		{"TZA","坦桑尼亚"},
		{"THA","泰国"},
		{"TGO","多哥"},
		{"TKL","托克劳"},
		{"TON","汤加"},
		{"TTO","特立尼达和多巴哥"},
		{"TUN","突尼斯"},
		{"TUR","土耳其"},
		{"TKM","土库曼斯坦"},
		{"TCA","特克斯科斯群岛"},
		{"TUV","图瓦卢"},
		{"UGA","乌干达"},
		{"UKR","乌克兰"},
		{"ARE","阿联酋"},
		{"GBR","英国"},
		{"USA","美国"},
		{"UMI","美国本土外小岛屿"},
		{"URY","乌拉圭"},
		{"UZB","乌兹别克斯坦"},
		{"VUT","瓦努阿图"},
		{"VAT","梵蒂冈"},
		{"VEN","委内瑞拉"},
		{"VNM","越南"},
		{"VGB","英属维尔京群岛"},
		{"VIR","美属维尔京群岛"},
		{"WLF","瓦利斯和富图纳"},
		{"ESH","西撒哈拉"},
		{"YEM","也门"},
		{"YUG","南斯拉夫"},
		{"ZMB","赞比亚"},
		{"ZWE","津巴布韦"}
	};
		
	public static String getSex(byte bsex[]) {
		String StrSexInfo = "";
		if (bsex[0] == 0x30) {
			StrSexInfo = "未知";
		} else if (bsex[0] == 0x31) {
			StrSexInfo = "男";
		} else if (bsex[0] == 0x32) {
			StrSexInfo = "女";
		} else if (bsex[0] == 0x39) {
			StrSexInfo = "未说明";
		} else {
			StrSexInfo = " ";
		}
		return StrSexInfo;
	}

	public static String getNation(byte bNationinfo[]) {
		String StrNation = "";
		int nNationNo = 0;
		nNationNo = (bNationinfo[0] - 0x30) * 10 + bNationinfo[2] - 0x30;
		switch (nNationNo) {
		case 1:
			StrNation = "汉";
			break;
		case 2:
			StrNation = "蒙古";
			break;
		case 3:
			StrNation = "回";
			break;
		case 4:
			StrNation = "藏";
			break;
		case 5:
			StrNation = "维吾尔";
			break;
		case 6:
			StrNation = "苗";
			break;
		case 7:
			StrNation = "彝";
			break;
		case 8:
			StrNation = "壮";
			break;
		case 9:
			StrNation = "布依";
			break;
		case 10:
			StrNation = "朝鲜";
			break;
		case 11:
			StrNation = "满";
			break;
		case 12:
			StrNation = "侗";
			break;
		case 13:
			StrNation = "瑶";
			break;
		case 14:
			StrNation = "白";
			break;
		case 15:
			StrNation = "土家";
			break;
		case 16:
			StrNation = "哈尼";
			break;
		case 17:
			StrNation = "哈萨克";
			break;
		case 18:
			StrNation = "傣";
			break;
		case 19:
			StrNation = "黎";
			break;
		case 20:
			StrNation = "傈僳";
			break;
		case 21:
			StrNation = "佤";
			break;
		case 22:
			StrNation = "畲";
			break;
		case 23:
			StrNation = "高山";
			break;
		case 24:
			StrNation = "拉祜";
			break;
		case 25:
			StrNation = "水";
			break;
		case 26:
			StrNation = "东乡";
			break;
		case 27:
			StrNation = "纳西";
			break;
		case 28:
			StrNation = "景颇";
			break;
		case 29:
			StrNation = "柯尔克孜";
			break;
		case 30:
			StrNation = "土";
			break;
		case 31:
			StrNation = "达斡尔";
			break;
		case 32:
			StrNation = "仫佬";
			break;
		case 33:
			StrNation = "羌";
			break;
		case 34:
			StrNation = "布朗";
			break;
		case 35:
			StrNation = "撒拉";
			break;
		case 36:
			StrNation = "毛南";
			break;
		case 37:
			StrNation = "仡佬";
			break;
		case 38:
			StrNation = "锡伯";
			break;
		case 39:
			StrNation = "阿昌";
			break;
		case 40:
			StrNation = "普米";
			break;
		case 41:
			StrNation = "塔吉克";
			break;
		case 42:
			StrNation = "怒";
			break;
		case 43:
			StrNation = "乌孜别克";
			break;
		case 44:
			StrNation = "俄罗斯";
			break;
		case 45:
			StrNation = "鄂温克";
			break;
		case 46:
			StrNation = "德昂";
			break;
		case 47:
			StrNation = "保安";
			break;
		case 48:
			StrNation = "裕固";
			break;
		case 49:
			StrNation = "京";
			break;
		case 50:
			StrNation = "塔塔尔";
			break;
		case 51:
			StrNation = "独龙";
			break;
		case 52:
			StrNation = "鄂伦春";
			break;
		case 53:
			StrNation = "赫哲";
			break;
		case 54:
			StrNation = "门巴";
			break;
		case 55:
			StrNation = "珞巴";
			break;
		case 56:
			StrNation = "基诺";
			break;
		case 57:
			StrNation = "其他";
			break;
		case 58:
			StrNation = "外国血统中国籍人士";
			break;
		default:
			StrNation = " ";
			break;
		}
		return StrNation;
	}

	private static String getCountryParam(String Code)
	{
		int i= 0;
		for(i=0;i<239;i++)
		{
			if(Code.equals(Country[i][0])){
				return Country[i][1];
			}
		}
		return "";
	}
	
	public static byte[] CombinationWltData(byte[] pucCHMsg, int[] puiCHMsgLen, byte[] pucPHMsg, int[] puiPHMsgLen){
		byte[] wltData = new byte[1384];
    	byte headData[] = {(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,(byte)0x69,(byte)0x05,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x90,(byte)0x01,(byte)0x00,(byte)0x04,(byte)0x00};
    	Arrays.fill(wltData, (byte)0);
		System.arraycopy(headData, 0, wltData, 0, headData.length);
		
		int pos = headData.length;
        System.arraycopy(pucCHMsg, 0 , wltData, pos, puiCHMsgLen[0]);
        pos += puiCHMsgLen[0];
        System.arraycopy(pucPHMsg, 0 , wltData, pos, puiPHMsgLen[0]);
        
    	return wltData;
	}
	
	public static HashMap<Integer, String> AnysizeData(byte[] idcMsg) throws UnsupportedEncodingException{
	
		HashMap<Integer, String> resultData = new HashMap<>();
		byte[] type = new byte[2];
		System.arraycopy(idcMsg,248,type,0,2);
		String sidtype = "";
		sidtype = new String(type,"UTF-16LE").trim();
		
		byte szName[] 		= new byte[30];
		byte szSex[] 		= new byte[128];
		byte szNation[] 	= new byte[128];
		byte szBirth[] 		= new byte[128];
		byte szAddress[] 	= new byte[128];
		byte szIDNo[] 		= new byte[36];
		byte szDepartment[] = new byte[128];
		byte szDateStart[] 	= new byte[128];
		byte szDateEnd[] 	= new byte[128];
		/*外国人多出数据*/
		byte szFGRName[]	= new byte[128];  	//英文名
		byte szCountryCode[]= new byte[128];  	//国籍或所在地区代码
		byte szVersion[]	= new byte[128];  	//证件版本号
		byte szIDType[]		= new byte[128];  	//证件类型标识
		byte szSLJGCode[]	= new byte[128];  	//当次申请受理机关代码
		/*港澳居民居住证多出数据*/
		byte szQFCS[]		= new byte[128];	//签发次数
		byte zsTXZHM[]		= new byte[128];	//通行证号码
		/*预留数据*/
		byte szReserved1[]	= new byte[8];
		byte szReserved2[]	= new byte[8];
		byte szReserved3[]	= new byte[8];
		
		if(sidtype.equals("I")){
			System.arraycopy(idcMsg,0,szFGRName,0,120);
			System.arraycopy(idcMsg,120,szSex,0,2);
			System.arraycopy(idcMsg,122,szIDNo,0,30);
			System.arraycopy(idcMsg,152,szCountryCode,0,6);
			System.arraycopy(idcMsg,158,szName,0,30);
			System.arraycopy(idcMsg,188,szDateStart,0,16);
			System.arraycopy(idcMsg,204,szDateEnd,0,16);
			System.arraycopy(idcMsg,220,szBirth,0,16);
			System.arraycopy(idcMsg,236,szVersion,0,4);
			System.arraycopy(idcMsg,240,szSLJGCode,0,8);
			System.arraycopy(idcMsg,248,szIDType,0,2);
			System.arraycopy(idcMsg,250,szReserved1,0,6);
			
			resultData.put(CARDTYPE_INDEX,"FGR");
			
			resultData.put(FGRNAME_INDEX,"英文名:" + new String(szFGRName,"UTF-16LE").trim());
			resultData.put(NAME_INDEX,"中文名:" + new String(szName,"UTF-16LE").trim());
			resultData.put(SEX_INDEX,"性别:"+getSex(szSex));
			resultData.put(BIRTH_INDEX,"出生日期:"+new String(szBirth,"UTF-16LE").trim());
			resultData.put(COUNTRY_INDEX,"国籍或所在地区代码:"+ getCountryParam(new String(szCountryCode,"UTF-16LE").trim()));
			resultData.put(IDNUM_INDEX,"永久居留证号码:"+new String(szIDNo,"UTF-16LE").trim());
			resultData.put(DEPART_INDEX,"签发机关:"+"公安部/Ministry of Public Security");
			resultData.put(VERSION_INDEX,"证件版本:"+new String(szVersion,"UTF-16LE").trim());
			resultData.put(IDTYPE_INDEX,"证件类型标识:"+new String(szIDType,"UTF-16LE").trim());
			resultData.put(SLJGCODE_INDEX,"当次受理机关代码:"+new String(szSLJGCode,"UTF-16LE").trim());
						
			resultData.put(STATIME_INDEX,"有效期起:"+new String(szDateStart,"UTF-16LE").trim());
			resultData.put(ENDTIME_INDEX,"有效期止:"+new String(szDateEnd,"UTF-16LE").trim());
			
		}else if(sidtype.equals("J")){
			System.arraycopy(idcMsg,0,szName,0,30);
			System.arraycopy(idcMsg,30,szSex,0,2);
			System.arraycopy(idcMsg,32,szReserved1,0,4);		//港澳居民居住证原民族信息
			System.arraycopy(idcMsg,36,szBirth,0,16);
			System.arraycopy(idcMsg,52,szAddress,0,70);
			System.arraycopy(idcMsg,122,szIDNo,0,36);
			System.arraycopy(idcMsg,158,szDepartment,0,30);
			System.arraycopy(idcMsg,188,szDateStart,0,16);
			System.arraycopy(idcMsg,204,szDateEnd,0,16);
			System.arraycopy(idcMsg,220,zsTXZHM,0,18);
			System.arraycopy(idcMsg,238,szQFCS,0,4);
			System.arraycopy(idcMsg,242,szReserved2,0,6);
			System.arraycopy(idcMsg,248,szIDType,0,2);
			System.arraycopy(idcMsg,250,szReserved3,0,6);
			
			resultData.put(CARDTYPE_INDEX,"GAT");
			
			resultData.put(NAME_INDEX,"中文名:" + new String(szName,"UTF-16LE"));
			resultData.put(SEX_INDEX,"性别:"+getSex(szSex));
			resultData.put(BIRTH_INDEX,"出生日期:"+new String(szBirth,"UTF-16LE"));
			resultData.put(ADDR_INDEX,"住址:"+new String(szAddress,"UTF-16LE"));
			resultData.put(IDNUM_INDEX,"公民身份证号码:"+new String(szIDNo,"UTF-16LE"));
			resultData.put(DEPART_INDEX,"签发机关:"+new String(szDepartment,"UTF-16LE"));
			resultData.put(TXZHM_INDEX,"通行证号码:"+new String(zsTXZHM,"UTF-16LE"));
			resultData.put(QFCS_INDEX,"签发次数:"+new String(szQFCS,"UTF-16LE"));
			resultData.put(IDTYPE_INDEX,"证件类型标识:"+new String(szIDType,"UTF-16LE"));
			
			resultData.put(STATIME_INDEX,"有效期起:"+new String(szDateStart,"UTF-16LE"));
			resultData.put(ENDTIME_INDEX,"有效期止:"+new String(szDateEnd,"UTF-16LE"));
		}else{
			System.arraycopy(idcMsg,0,szName,0,30);
			System.arraycopy(idcMsg,30,szSex,0,2);
			System.arraycopy(idcMsg,32,szNation,0,4);
			System.arraycopy(idcMsg,36,szBirth,0,16);
			System.arraycopy(idcMsg,52,szAddress,0,70);
			System.arraycopy(idcMsg,122,szIDNo,0,36);
			System.arraycopy(idcMsg,158,szDepartment,0,30);
			System.arraycopy(idcMsg,188,szDateStart,0,16);
			System.arraycopy(idcMsg,204,szDateEnd,0,16);
			
			resultData.put(CARDTYPE_INDEX,"CHN");
			
			resultData.put(NAME_INDEX,"中文名:" + new String(szName,"UTF-16LE"));
			resultData.put(SEX_INDEX,"性别:"+getSex(szSex));
			resultData.put(NATION_INDEX,"民族:"+getNation(szNation));
			resultData.put(BIRTH_INDEX,"出生日期:"+new String(szBirth,"UTF-16LE"));
			resultData.put(ADDR_INDEX,"住址:"+new String(szAddress,"UTF-16LE"));
			resultData.put(IDNUM_INDEX,"公民身份证号码:"+new String(szIDNo,"UTF-16LE"));
			resultData.put(DEPART_INDEX,"签发机关:"+new String(szDepartment,"UTF-16LE"));
			resultData.put(STATIME_INDEX,"有效期起:"+new String(szDateStart,"UTF-16LE"));
			resultData.put(ENDTIME_INDEX,"有效期止:"+new String(szDateEnd,"UTF-16LE"));
		}
		
		return resultData;
	}
}
