$(function () {
	var vm = new Vue({
		el:'#rrapp',
		data:{
			q:{
				conName: null,
				url: null,
				port: "3306",
				database: null,
				usrName: null,
				password: null,
				packageName: null,
				moduleName: null,
				author: null,
				email: null
			}
		},
		methods: {
			getHistory: function () {
				window.location.href = "historyConnection.html"
			},
			getConnection: function() {
				var connectionMsg = vm.q;
				var checkMessageResult = checkMessage(vm.q);
				if(!checkMessageResult.success){
					layer.alert(checkMessageResult.msg);
					return;
				}
			    $.ajax({
			    	type : 'POST',
					url  : 'sys/generator/getConnection',
					contentType:"application/json",
					data : JSON.stringify(connectionMsg),
					dataType : "json", 
					success:function(data){
						var code = data.code;
						if("500" == code){
							layer.alert("连接异常");
						}else{
                            save(connectionMsg);
                            window.sessionStorage.setItem("con",JSON.stringify(connectionMsg));
							window.location.href = "generator.html"
						}
					}
			    });
			},
			save: function(){
				var data = vm.q;
				var checkMessageResult = checkMessage(data);
				if(!checkMessageResult.success){
					layer.alert(checkMessageResult.msg);
					return;
				}
				save(data);
				layer.msg("保存成功");
			},
			reset: function(){
				window.sessionStorage.removeItem("con");
				window.location.href = "connection.html"
			}
		}
	});
	
	var con = JSON.parse(window.sessionStorage.getItem("con"));
	if(con != null && con != ""){
		vm.q.conName = con.conName;
		vm.q.url = con.url;
		vm.q.port = con.port;
		vm.q.database = con.database;
		vm.q.usrName = con.usrName;
		vm.q.password = con.password;
		vm.q.packageName = con.packageName;
		vm.q.moduleName = con.moduleName;
		vm.q.author = con.author;
		vm.q.email = con.email;
	}
});

function save(data){
    if(historyConnection == null){
        historyConnection=new Array();
    }
	//连接名如果已经存在则删除
    for(var i=0;i<historyConnection.length;i++){
    	var his = historyConnection[i];
    	if(his.conName == data.conName){
            historyConnection.splice(i,1);
            break;
		}
	}
	historyConnection.push(data);
    window.localStorage.setItem("historyConnection",JSON.stringify(historyConnection));
}

function checkMessage(con){
	var checkResult = {};
	if(con == null){
		checkResult.msg = "连接信息获取异常";
		checkResult.success = false;
	}else if(con.conName == null || con.conName == ""){
		checkResult.msg = "连接名不能为空";
		checkResult.success = false;
	}else if(con.url == null || con.url == ""){
		checkResult.msg = "主机名或IP地址不能为空";
		checkResult.success = false;
	}else if(con.port == null || con.port == ""){
		checkResult.msg = "端口号不能为空";
		checkResult.success = false;
	}else if(con.database == null || con.database == ""){
		checkResult.msg = "数据库不能为空";
		checkResult.success = false;
	}else if(con.usrName == null || con.usrName == ""){
		checkResult.msg = "用户名不能为空";
		checkResult.success = false;
	}else if(con.password == null || con.password == ""){
		checkResult.msg = "密码不能为空";
		checkResult.success = false;
	}else{
		checkResult.msg = "校验成功";
		checkResult.success = true;
	}
	return checkResult;
}




