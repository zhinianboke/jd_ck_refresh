const fetch = require('node-fetch')


async function sleepAsync(time) {    await sleep(time);}

function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}    
        
const timer = ms => new Promise( res => setTimeout(res, ms));

// 变量，填写手机号，使用  @分割  例如：123@456
var phoneStr = "123@456";
// 车队,可填写 ark.leafxxx.win  或者 login.ouklc.com
var serverUrl = "ark.leafxxx.win";


var codeJiuxu = false;
var code = "";
main()
async function main() {
    try{
        var phoneArr = phoneStr.split("@");
        for(var i=0;i<phoneArr.length;i++) {
            var phone = phoneArr[i];
            codeJiuxu = false;
            code = "";
	        console.log(phone)
	        await sendCode(phone)
            
	        if(codeJiuxu) {
	            for(var j=0;j < 15;j++) {
	                if(code) {
	                    break;
	                }
	                console.log(phone + "开始第"+(j+1)+"次获取验证码");
	                await sleepAsync(1500);
	                if(!code) {
	                    await getCode(phone);
	                }
	            }
	            if(code) {
	                await loginJd(phone, code);
	            }
	        }
	        
        }
	   // 
	}
	catch(e) {
	    
	}
}
        
async function sendCode(phone) {
    console.log(phone + "开始发送验证码");
	var response = await fetch("https://dy.zhinianboke.com/jd/sendCode?phone="+phone+"&serverUrl="+serverUrl, {
      "headers": {
      },
      "body": "",
      "method": "post"
    });
	await response.json().then((data) => {
		if(data.status == 200) {
		    codeJiuxu = true;
		    console.log(phone + "验证码发送成功");
		}
		else {
		    console.log(phone + data.message);
		}
	}).catch((err) => {
		console.log(err);
	})
}
async function getCode(phone) {
	var response = await fetch("https://dy.zhinianboke.com/jd/getCode?phone="+phone+"&serverUrl="+serverUrl, {
      "headers": {
      },
      "body": "",
      "method": "post"
    });
	await response.json().then((data) => {
		if(data.status == 200) {
		    code = data.data;
            console.log(phone + "获取的验证码为：" + code);
		}
		else {
            console.log(phone + "本次未获得验证码");
		}
	}).catch((err) => {
		console.log(err);
	})
}
async function loginJd(phone,code) {
	var response = await fetch("https://dy.zhinianboke.com/jd/loginJd?phone="+phone + "&code="+code+"&serverUrl="+serverUrl, {
      "headers": {
      },
      "body": "",
      "method": "post"
    });
	await response.json().then((data) => {
		console.log(phone + data.message);
	}).catch((err) => {
		console.log(err);
	})
}
