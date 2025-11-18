# WoowaLang

우아한테크코스 전용 한국어 프로그래밍 언어 인터프리터

## 📖 소개

WoowaLang은 Crafting Interpreters를 기반으로 만든 한국어 키워드 프로그래밍 언어입니다.
Java로 구현된 인터프리터로, 변수, 함수, 클래스 등의 기본적인 프로그래밍 기능을 지원합니다.

## 🚀 시작하기

### 프로젝트 구조
```
WoowaLang/
├── src/
│   ├── woowa/          # 인터프리터 핵심 코드
│   ├── function/       # 내장 함수들
│   └── tool/           # AST 생성 도구
├── examples/           # 예제 코드
```

## 🛠️ 빌드 및 실행

### 1. 컴파일

#### 전체 프로젝트 컴파일
```bash
# out 디렉토리 생성
mkdir -p out/production/WoowaLang

# 모든 Java 파일 컴파일
javac -d out/production/WoowaLang -encoding UTF-8 src/**/*.java src/**/**/*.java
```

#### AST 클래스 생성 (개발 시)
AST(Abstract Syntax Tree) 클래스를 재생성해야 할 경우:

```bash
# GenerateAst 컴파일
javac -d out/production/WoowaLang src/tool/GenerateAst.java

# Expr.java와 Stmt.java 생성
java -cp out/production/WoowaLang tool.GenerateAst src/woowa
```

#### 3. REPL 모드 (대화형)
```bash
java -cp out/production/WoowaLang woowa.Woowa
```

### 4. WoowaLang 실행

#### 계산기 예제
```bash
java -cp out/production/WoowaLang woowa.Woowa examples/{파일명}.woowa
```


## 🧪 예제 실행하기

### 피보나치 수열
```bash
java -cp out/production/WoowaLang woowa.Woowa examples/fibonacci.woowa
```

### 배열 테스트
```bash
java -cp out/production/WoowaLang woowa.Woowa examples/arrayTest.woowa
```

### 계산기 (프리코스 과제 구현)
```bash
java -cp out/production/WoowaLang woowa.Woowa examples/calculator.woowa
```

실행 후:
```
덧셈할 문자열을 입력해 주세요.
>> 1,2,3
결과 : 6
```

## 📚 언어 기능

### 키워드
| 한글 | 영문 | 설명 |
|------|------|------|
| `변수` | `var` | 변수 선언 |
| `함수` | `fun` | 함수 선언 |
| `클래스` | `class` | 클래스 선언 |
| `만약` | `if` | 조건문 |
| `아니면` | `else` | else 문 |
| `반복` | `for` | for 반복문 |
| `하는동안` | `while` | while 반복문 |
| `반환` | `return` | 함수 반환 |
| `출력` | `print` | 콘솔 출력 |
| `참` | `true` | 불리언 true |
| `거짓` | `false` | 불리언 false |
| `널` | `nil` | null 값 |
| `그리고` | `and` | 논리 AND |
| `또는` | `or` | 논리 OR |
| `자신` | `this` | 인스턴스 참조 |
| `상위` | `super` | 상위 클래스 참조 |

### 내장 함수
- `읽기()` - 사용자 입력 받기
- `길이(문자열)` - 문자열 길이
- `부분문자열(문자열, 시작, 끝)` - 문자열 자르기
- `분리(문자열, 구분자)` - 문자열 분리 (정규식 지원)
- `정수파싱(문자열)` - 문자열을 정수로 변환
- `배열크기(배열)` - 배열 크기
- `배열추가(배열, 요소)` - 배열에 요소 추가
- `포함(배열, 요소)` - 배열에 요소가 있는지 확인
- `시계()` - 현재 시간 (Unix timestamp)

## 🐛 트러블슈팅

### 한글이 깨져요
컴파일 시 UTF-8 인코딩 옵션을 추가하세요:
```bash
javac -d out/production/WoowaLang -encoding UTF-8 src/**/*.java src/**/**/*.java
```

### 클래스 파일을 찾을 수 없어요
`out/production/WoowaLang` 디렉토리가 존재하는지 확인하고, 컴파일을 다시 실행하세요.

### 예제 파일이 실행되지 않아요
파일 경로가 올바른지 확인하세요. 프로젝트 루트에서 실행해야 합니다:
```bash
java -cp out/production/WoowaLang woowa.Woowa examples/calculator.woowa
```

## 🎯 프로젝트 목표

이 프로젝트는 우아한테크코스 프리코스 경험을 바탕으로:
1. 프로그래밍 언어의 작동 원리 이해
2. 인터프리터 구현 경험
3. 한국어 프로그래밍 언어의 가능성 탐구

를 목표로 만들어졌습니다.

## 📄 라이선스

이 프로젝트는 교육 목적으로 제작되었습니다.

## 🙏 감사의 글

- **Crafting Interpreters** (Robert Nystrom) - 인터프리터 설계의 기본 구조
- **우아한테크코스** - 프로젝트 아이디어와 동기 부여