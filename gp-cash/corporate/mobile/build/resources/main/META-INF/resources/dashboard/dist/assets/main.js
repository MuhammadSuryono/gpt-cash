$(document).ready(function () {

    $("#btnSubmit").click(function (event) {

        // stop submit the form, we will post it manually.
        event.preventDefault();

        fire_ajax_upload();
    });

});

function fire_ajax_upload() {

    // Get form
    var form = $('#fileUploadForm')[0];

    var data = new FormData(form);

    data.append("CustomField", "This is some extra data, testing");

    $("#btnSubmit").prop("disabled", true);

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "/corp/upload",
        data: data,
        processData: false, // prevent jQuery from automatically transforming
							// the data into a query string
        contentType: false,
        cache: false,
        timeout: 600000,
        success: function (data) {

            $("#result").text(JSON.stringify(data));
            console.log("SUCCESS : ", data);
            $("#btnSubmit").prop("disabled", false);

            fire_ajax_submit();
        },
        error: function (e) {

            $("#result").text(e.responseText);
            console.log("ERROR : ", e);
            $("#btnSubmit").prop("disabled", false);

        }
    });
}

function fire_ajax_submit() {
    $.ajax({
        type: "POST",
        url: "/corp/afterUpload",
        data: "{}",
        processData: false,
        contentType: "application/json",
        cache: false,
        timeout: 600000,
        success: function (data) {
            $("#result2").text(JSON.stringify(data));
            console.log("SUCCESS : ", data);
            $("#btnSubmit").prop("disabled", false);
        },
        error: function (e) {
            $("#result2").text(e.responseText);
            console.log("ERROR : ", e);
            $("#btnSubmit").prop("disabled", false);

        }
    });   
}