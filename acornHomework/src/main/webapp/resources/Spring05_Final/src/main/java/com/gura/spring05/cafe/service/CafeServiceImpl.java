package com.gura.spring05.cafe.service;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.gura.spring05.cafe.dao.CafeCommentDao;
import com.gura.spring05.cafe.dao.CafeDao;
import com.gura.spring05.cafe.dto.CafeCommentDto;
import com.gura.spring05.cafe.dto.CafeDto;
import com.gura.spring05.exception.NotDeleteException;
import com.gura.spring05.file.dto.FileDto;


@Service
public class CafeServiceImpl  implements CafeService{
	@Autowired private CafeDao cafeDao;
	@Autowired private CafeCommentDao cafeCommentDao;

	//한 페이지에 나타낼 row 의 갯수
	final int PAGE_ROW_COUNT=5;
	//하단 디스플레이 페이지 갯수
	final int PAGE_DISPLAY_COUNT=5;
	
	@Override
	public void getList(HttpServletRequest request) {
		//보여줄 페이지의 번호
		int pageNum=1;
		//보여줄 페이지의 번호가 파라미터로 전달되는지 읽어와 본다.	
		String strPageNum=request.getParameter("pageNum");
		if(strPageNum != null){//페이지 번호가 파라미터로 넘어온다면
			//페이지 번호를 설정한다.
			pageNum=Integer.parseInt(strPageNum);
		}
		//보여줄 페이지 데이터의 시작 ResultSet row 번호
		int startRowNum=1+(pageNum-1)*PAGE_ROW_COUNT;
		//보여줄 페이지 데이터의 끝 ResultSet row 번호
		int endRowNum=pageNum*PAGE_ROW_COUNT;
		/*
			검색 키워드에 관련된 처리 
		*/
		String keyword=request.getParameter("keyword");
		String condition=request.getParameter("condition");
		if(keyword==null){//전달된 키워드가 없다면 
			keyword=""; //빈 문자열을 넣어준다. 
			condition="";
		}
		//인코딩된 키워드를 미리 만들어 둔다. 
		String encodedK=URLEncoder.encode(keyword);
		
		//검색 키워드와 startRowNum, endRowNum 을 담을 FileDto 객체 생성
		CafeDto dto = new CafeDto();
		dto.setStartRowNum(startRowNum);
		dto.setEndRowNum(endRowNum);
		
		if(!keyword.equals("")){ //만일 키워드가 넘어온다면 
			if(condition.equals("title_content")){
				//검색 키워드를 FileDto 객체의 필드에 담는다. 
				dto.setTitle(keyword);
				dto.setContent(keyword);

			}else if(condition.equals("title")){
				dto.setTitle(keyword);
				
			}else if(condition.equals("writer")){
				dto.setWriter(keyword);
				
			}
		}
		
		//파일 목록 얻어오기
		List<CafeDto> list = cafeDao.getList(dto);
		
		//전체 row 의 갯수를 담을 변수 
		int totalRow=cafeDao.getCount(dto);
		
		//전체 페이지의 갯수 구하기
		int totalPageCount=
				(int)Math.ceil(totalRow/(double)PAGE_ROW_COUNT);//정수를 정수로 나눠봐야 실수가 아니기 때문에 pageNum = 1~5 사이에서는 다 1페이지가 시작 번호
		//시작 페이지 번호
		int startPageNum=   
			1+((pageNum-1)/PAGE_DISPLAY_COUNT)*PAGE_DISPLAY_COUNT;
		//끝 페이지 번호
		int endPageNum=startPageNum+PAGE_DISPLAY_COUNT-1;
		//끝 페이지 번호가 잘못된 값이라면   항상 글의 갯수가 맞춰져 있지 않기 떄문에 있는 만큼의 페이지를 끝 페이지로 설정한다.
		if(totalPageCount < endPageNum){
			endPageNum=totalPageCount; //보정해준다. 
		}
		
		//	EL에서 사용할 값을 미리 request에 담아두기
		request.setAttribute("list", list); 
		request.setAttribute("startPageNum", startPageNum);
		request.setAttribute("endPageNum", endPageNum);
		request.setAttribute("pageNum", pageNum);
		request.setAttribute("totalPageCount", totalPageCount);
		request.setAttribute("condition", condition);
		request.setAttribute("keyword", keyword);
		request.setAttribute("encodedK", encodedK);
	} 

	@Override
	public void getDetail(HttpServletRequest request) {
		//파라미터로 전달되는 글번호 
		int num=Integer.parseInt(request.getParameter("num"));
		/*
		검색 키워드에 관련된 처리 
		*/
		String keyword=request.getParameter("keyword"); //검색 키워드
		String condition=request.getParameter("condition"); //검색 조건
		if(keyword==null){//전달된 키워드가 없다면 
			keyword=""; //빈 문자열을 넣어준다. 
			condition="";
		}
		//인코딩된 키워드를 미리 만들어 둔다. 
		String encodedK=URLEncoder.encode(keyword);
		
		//글번호와 검색 키워드를 담을 CafeDto 객체 생성
		CafeDto dto=new CafeDto();
		dto.setNum(num);//글번호 담기 
		
		if(!keyword.equals("")){ //만일 키워드가 넘어온다면 
			if(condition.equals("title_content")){
				//검색 키워드를 FileDto 객체의 필드에 담는다. 
				dto.setTitle(keyword);
				dto.setContent(keyword);	
			}else if(condition.equals("title")){
				dto.setTitle(keyword);
			}else if(condition.equals("writer")){
				dto.setWriter(keyword);
			}
		}
		//자세히 보여줄 글 정보 
		CafeDto resultDto=cafeDao.getData(dto);
		
		//view 페이지에서 필요한 내용 HttpServletRequest 에 담기
		request.setAttribute("dto", resultDto);
		request.setAttribute("condition", condition);
		request.setAttribute("keyword", keyword);
		request.setAttribute("encodedK", encodedK);
		
		//글 조회수 올리기
		cafeDao.addViewCount(num);
		
		/* 아래는 댓글 페이징 처리 관련 비즈니스 로직 입니다.*/
		final int PAGE_ROW_COUNT=5;
		final int PAGE_DISPLAY_COUNT=5;
		
		
		//전체 row 의 갯수를 읽어온다.
		//자세히 보여줄 글의 번호가 ref_group  번호 이다. 
		int totalRow=cafeCommentDao.getCount(num);
		
		//보여줄 페이지의 번호(만일 pageNum 이 넘어오지 않으면 가장 마지막 페이지)
		String strPageNum=request.getParameter("pageNum");
		//전체 페이지의 갯수 구하기
		int totalPageCount=
						(int)Math.ceil(totalRow/(double)PAGE_ROW_COUNT);
		int pageNum=totalPageCount;
	
		if(strPageNum!=null) {
			pageNum=Integer.parseInt(strPageNum);
		}
		//보여줄 페이지 데이터의 시작 ResultSet row 번호
		int startRowNum=1+(pageNum-1)*PAGE_ROW_COUNT;  
		//보여줄 페이지 데이터의 끝 ResultSet row 번호  
		int endRowNum=pageNum*PAGE_ROW_COUNT;			   
		
		
		//시작 페이지 번호
		int startPageNum=
			1+((pageNum-1)/PAGE_DISPLAY_COUNT)*PAGE_DISPLAY_COUNT;
		//끝 페이지 번호
		int endPageNum=startPageNum+PAGE_DISPLAY_COUNT-1;
		//끝 페이지 번호가 잘못된 값이라면 
		if(totalPageCount < endPageNum){
			endPageNum=totalPageCount; //보정해준다. 
		}
		
		// CafeCommentDto 객체에 위에서 계산된 startRowNum 과 endRowNum 을 담는다.
		CafeCommentDto commentDto=new CafeCommentDto();
		commentDto.setStartRowNum(startRowNum);
		commentDto.setEndRowNum(endRowNum);
		//ref_group 번호도 담는다.
		commentDto.setRef_group(num);
		
		//DB 에서 댓글 목록을 얻어온다.
		List<CafeCommentDto> commentList=cafeCommentDao.getList(commentDto);
		//request 에 담아준다.
		request.setAttribute("commentList", commentList);
		request.setAttribute("totalPageCount", totalPageCount);
		request.setAttribute("startPageNum", startPageNum);
		request.setAttribute("endPageNum", endPageNum);
		request.setAttribute("pageNum", pageNum);
	}

	@Override
	public void insert(CafeDto dto) {
		cafeDao.insert(dto);
	}

	@Override
	public void delete(int num, HttpServletRequest request) {
//		int num = Integer.parseInt(request.getParameter("num"));
//		CafeDto dto = cafeDao.getData(num);
//		String id = (String)request.getSession().getAttribute("id");
//		if(!id.equals(dto.getWriter())) {
//			throw new NotDeleteException("가세요 ㅎㅎ");
//		}
		//aspect로 대체합니다.

		cafeDao.delete(num);
	}

	@Override
	public void updateForm(CafeDto dto, ModelAndView m) {
		
		dto = cafeDao.getData(dto);
		
		m.addObject("dto", dto);
	}

	@Override
	public void update(CafeDto dto) {
		cafeDao.update(dto);
	}

	@Override
	public void saveComment(HttpServletRequest request) {
		//댓글 작성자
		String writer = (String)request.getSession().getAttribute("id");
		//폼 전송되는 댓글의 정보 얻어내기
		int ref_group = Integer.parseInt(request.getParameter("ref_group"));
		String target_id = request.getParameter("target_id");
		String content = request.getParameter("content");
		/*
		 * 원글의 댓글은 comment_group 번호가 전송이 안되고
		 * 리플은 comment_group 번호가 전송이 된다.
		 * 따라서 null 여부를 조사하면 원글의 댓글인지 리플인지 판단할 수 있다.
		 * */
		String comment_group = request.getParameter("comment_group");
		//새 댓글의 글 번호는 dao를 이용해서 미리 얻어낸다.
		int seq = cafeCommentDao.getSequence();
		
		//저장할 댓글 정보를 dto에 담기
		CafeCommentDto dto = new CafeCommentDto();
		dto.setNum(seq);
		dto.setWriter(writer);
		dto.setRef_group(ref_group);
		dto.setTarget_id(target_id);
		dto.setContent(content);
		
		if(comment_group == null) {//원글의 댓글
			dto.setComment_group(seq);
		}else {//리플
			//폼 전송된 comment_group번호를 숫자로 바꿔서 dto에 넣어준다.
			dto.setComment_group(Integer.parseInt(comment_group));
		}
		//댓글 정보를 db에 저장한다.
		cafeCommentDao.insert(dto);
		
	}

	@Override
	public void deleteComment(HttpServletRequest req) {
		int num = Integer.parseInt(req.getParameter("num"));
		//세션에 저장된 로그인 된 아이디
		String id = (String)req.getSession().getAttribute("id");
		//댓글의 정보를 얻어와서 댓글이 작성자와같은지 비교한다.
		String writer = cafeCommentDao.getData(num).getWriter();
		if(id.equals(writer)) {
			cafeCommentDao.delete(num);
		}else {
			throw new NotDeleteException("삭제할 수 없습니다.");
		}
	}

	
	@Override
	public void updateComment(CafeCommentDto dto) {
		cafeCommentDao.update(dto);
	}
	
	
}
