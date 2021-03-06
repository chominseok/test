<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<script src="${pageContext.request.contextPath }/resources/js/angular.min.js"></script>
<script src="${pageContext.request.contextPath }/resources/js/jquery-3.5.1.js"></script>
<link rel="stylesheet" href="../resources/css/bootstrap.css" />
</head>
<body ng-app>
	<div class="container">
		<h1 ng-init="a = 'btn-primary'">클래스 속성 조작하기</h1>
		<input type="text" ng-model="b"/>
		<button class="btn btn-primary">버튼1</button>
		<button class="btn {{a}}">버튼2</button>
		<button class="btn {{b}}">버튼3</button>
		<button ng-class="['btn','btn-primary']">버튼4</button>
		<button ng-class="{'btn':true,'btn-primary':true}">버튼5</button>
		<br>
		<hr>
		<br>
		<input type="checkbox" ng-model="isLarge"/>
		<button class="btn btn-success" ng-class="{'btn-danger':isLarge,'btn-lg':isLarge}">버튼6</button>
	</div>
</body>
</html>