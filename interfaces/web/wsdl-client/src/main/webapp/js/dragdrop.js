function var_dump(obj) 
{
    var out = '';
    for (var i in obj) {
        out += i + ": " + obj[i] + "\n";
    }
    return(out);
}

function getAbsolutePath() {
    var loc = window.location;
    var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
    return loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));
}

function sendFileToServer(formData, status, id) {
	var uploadURL = "UploadFiles";
	//alert(filename);
	//var extraData = {}; //Extra Data.
	var jqXHR = $.ajax({
		xhr : function() {
			var xhrobj = $.ajaxSettings.xhr();
			if (xhrobj.upload) {
				xhrobj.upload.addEventListener('progress', function(event) {
					var percent = 0;
					var position = event.loaded || event.position;
					var total = event.total;
					if (event.lengthComputable) {
						percent = Math.ceil(position / total * 100);
					}
					status.setProgress(percent);
				}, false);
			}
			return xhrobj;
		},
		url : uploadURL,
		type : "POST",
		contentType : false,
		processData : false,
		cache : false,
		data : formData,
		success : function(data) {
			var idName =  id.substr(0,id.indexOf('_'));
			var urlFile =  getAbsolutePath() + 'UploadFiles%3Ffilename%3D' + data;
			
			status.setProgress(100);
			if (data.length > 20)
				$("#" + id).html(data.substring(0,20) + '...');
			else 
				$("#" + id).html(data);
			$('#' + idName + '[name="' +  idName + '"]').val( urlFile );
		}
	});

	status.setAbort(jqXHR);
}
function destroyStatusbar(id) {
	$(id + "_statusbar").html('');
}
var rowCount = 0;
function createStatusbar(obj, id) {
	//destroyStatusbar(id);
	rowCount++;
	var row = "odd";
	if (rowCount % 2 == 0)
		row = "even";
	this.statusbar = $("<div class='statusbar " + row + "' id='" + id + "_statusbar'></div>");
	this.filename = $("<div class='filename'></div>").appendTo(this.statusbar);
	this.size = $("<div class='filesize'></div>").appendTo(this.statusbar);
	this.progressBar = $("<div class='progressBar'><div></div></div>").appendTo(this.statusbar);
	this.abort = $("<div class='abort'>Abort</div>").appendTo(this.statusbar);
	//obj.after(this.statusbar);
	obj.append(this.statusbar);
	this.setFileNameSize = function(name, size) {
		var sizeStr = "";
		var sizeKB = size / 1024;
		if (parseInt(sizeKB) > 1024) {
			var sizeMB = sizeKB / 1024;
			sizeStr = sizeMB.toFixed(2) + " MB";
		} else {
			sizeStr = sizeKB.toFixed(2) + " KB";
		}

		if (name.length > 20)
			this.filename.html(name.substring(0,20) + ' ...');
		else
			this.filename.html(name);
		this.size.html(sizeStr);
	};
	this.setProgress = function(progress) {
		var progressBarWidth = progress * this.progressBar.width() / 100;
		this.progressBar.find('div').animate({
			width : progressBarWidth
		}, 10).html(progress + "% ");
		if (parseInt(progress) >= 100) {
			this.abort.hide();
		}
	};
	this.setAbort = function(jqxhr) {
		var sb = this.statusbar;
		this.abort.click(function() {
			jqxhr.abort();
			sb.hide();
		});
	};
}
function handleFileUpload(files, obj, id) {
	//for ( var i = 0; i < files.length; i++) {
        //alert("aqui 3");
	if (files[0] != undefined)
	{
		var filename=files[0].name.replace(/[^a-z\.0-9\s]/gi, '').replace(/[_\s]/g, '');                                              
		var file=files[0];
		var fd = new FormData();
		var status = new createStatusbar(obj, id); //Using this we can set progress.
                
                var name;
                var extension;                                                                                
                
		file.name=filename;
		
		fd.append('file', file);                
		status.setFileNameSize(file.name, file.size);
		sendFileToServer(fd, status, id);
	}
}

function loadDragAndDrop()
{
	var objects = $(".dragandrophandler");
	$.each(objects, function (index, objs)
	{
		if (objs != undefined)
		{
			$(this).on('dragenter', function(e) {
				e.stopPropagation();
				e.preventDefault();
				$(this).css('border', '2px solid #525721');
			});
			$(this).on('dragover', function(e) {
				e.stopPropagation();
				e.preventDefault();
			});
			$(this).on('drop', function(e) {
				$(this).css('border', '2px dotted #525721');
				e.preventDefault();
				var files = e.originalEvent.dataTransfer.files;
				handleFileUpload(files, $(this), $(this).attr('id'));
			});
			$(document).on('dragenter', function(e) {
				e.stopPropagation();
				e.preventDefault();
			});
			$(document).on('dragover', function(e) {
				e.stopPropagation();
				e.preventDefault();
				$(this).css('border', '2px dotted #525721');
			});
			$(document).on('drop', function(e) {
				e.stopPropagation();
				e.preventDefault();
			});
		}

	});
}
