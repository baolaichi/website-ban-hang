<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
        <%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>


            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Create User</title>
                <!-- Latest compiled and minified CSS -->
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

                <!-- Latest compiled JavaScript -->
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

                <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
                <link rel="stylesheet" href="/css/styles.css" />
                <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>

                <script>
                    $(document).ready(() => {
                        const avatarFile = $("#avatarFile");
                        avatarFile.change(function (e) {
                            const imgURL = URL.createObjectURL(e.target.files[0]);
                            $("#avatarPreview").attr("src", imgURL);
                            $("#avatarPreview").css({ "display": "block" });
                        });
                    });
                </script>


            </head>

            <body class="sb-nav-fixed">
                <jsp:include page="../layout/header.jsp"/>  

                <div id="layoutSidenav">
                    <jsp:include page="../layout/sidebar.jsp"/>
                    <div id="layoutSidenav_content">
                        <main>
                            <div class="container mt-5">
                                <div class="row">
                                    <div class="col-md-6 col-12 mx-auto">
                                        <h1>Create User</h1>
                                        <hr />
                                        <form:form method="post" action="/admin/user/create" modelAttribute="newUser" class="row"
                                        enctype="multipart/form-data">

                                            <div class="mb-3 col-12 col-md-6">
                                                <c:set var="errorEmail">
                                                <form:errors path="email" cssClass="invalid-feedback" />
                                                </c:set>

                                                <label for="email" class="form-label">Email address:</label>
                                                <form:input type="email" 
                                                class="form-control ${not empty errorEmail ? 'is-invalid' : ''}" path="email" />
                                                ${errorEmail}
                                            </div>

                                            <div class="mb-3 col-12 col-md-6">
                                                <c:set var="errorPassword">
                                                    <form:errors path="password" cssClass="invalid-feedback" />
                                                </c:set>
                                                <label for="password" class="form-label">Password:</label>
                                                <form:input type="password"
                                                 class="form-control ${not empty errorPassword ? 'is-invalid' : ''}" path="password" />
                                                 ${errorPassword}
                                            </div>
                                            <div class="mb-3 col-12 col-md-6">
                                                <label for="examplePhone1" class="form-label">Phone number: </label>
                                                <form:input path="phone" type="text" class="form-control" />
                                            </div>
                                            <div class="mb-3 col-12 col-md-6">
                                                <c:set var = "errorFullName">
                                                    <form:errors path="fullName" cssClass="invalid-feedback" />
                                                </c:set>
                                                <label for="fullName" class="form-label">Full name: </label>
                                                <form:input path="fullName" type="text" class="form-control ${not empty errorFullName ? 'is-invalid' : ''}" />
                                                ${errorFullName}
                                            </div>
                                            <div class="mb-3 col-12 col-md-12">
                                                <label for="exampleAddress1" class="form-label">Address: </label>
                                                <form:input path="address" type="text" class="form-control" />
                                            </div>

                                            <div class="mb-3 col-12 col-md-6">
                                                <label class="form-label">Role:</label>
                                                <form:select class="form-select" path="role.name">
                                                    <form:option value="ADMIN">ADMIN</form:option>
                                                    <form:option value="USER">USER</form:option>
                                                </form:select>
                                            </div>
                                            <div class="mb-3 col-12 col-md-6">
                                                    <label for="avatarFile" class="form-label">Upload Avatar</label>
                                                    <input class="form-control" type="file" id="avatarFile" 
                                                    accept=".jpg, .png, .jpeg" name="avatarFile"/>
                                            </div>
                                            <div class="col-12 mb-3">
                                                <img style="max-height: 250px; display: none;" alt="avatar preview" id="avatarPreview"/>
                                            </div>
                                            <div class="col-12 mb-5">
                                                <button type="submit" class="btn btn-primary">Create</button>
                                            </div>
                                        </form:form>
                                    </div>
                                </div>
                        </main>
                    </div>
                </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
            crossorigin="anonymous"></script>
        <script src="/js/scripts.js"></script>
            </body>

            </html>