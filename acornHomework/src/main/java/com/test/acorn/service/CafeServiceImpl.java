package com.test.acorn.service;

import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.test.acorn.dao.CafeDao;
import com.test.acorn.dto.CafeDto;

@Service
public class CafeServiceImpl implements CafeService{
	@Autowired
	private CafeDao cafeDao;
	
	@Override
	public void inserForm(CafeDto dto) {
		cafeDao.insertForm(dto);
	}
	
	//한 페이지에 나타낼 row 의 갯수
	final int PAGE_ROW_COUNT=5;
	//하단 디스플레이 페이지 갯수
	final int PAGE_DISPLAY_COUNT=5;
	
	@Override
	public void getList(ModelAndView m, HttpServletRequest request) {
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
	public void detail(ModelAndView m, HttpServletRequest request) {
		int num = Integer.parseInt(request.getParameter("num")); 
		
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
		CafeDto resultDto=cafeDao.getData(num);
		
		//view 페이지에서 필요한 내용 HttpServletRequest 에 담기
		request.setAttribute("dto", resultDto);
		request.setAttribute("condition", condition);
		request.setAttribute("keyword", keyword);
		request.setAttribute("encodedK", encodedK);
		
		//글 조회수 올리기
		cafeDao.addViewCount(num);
	}

	@Override
	public void delete(int num) {
		cafeDao.delete(num);
	}

	@Override
	public void updateForm(int num, ModelAndView m) {
		CafeDto dto = cafeDao.getData(num);
		m.addObject("dto", dto);
	}

	@Override
	public void update(CafeDto dto) {
		cafeDao.updateForm(dto);
	}

	
}