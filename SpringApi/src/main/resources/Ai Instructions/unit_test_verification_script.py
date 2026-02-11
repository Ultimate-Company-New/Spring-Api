python3 - <<'PY'
import os,re
from pathlib import Path
from collections import Counter
from datetime import datetime

root = Path('/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi')
service_dir = root / 'src/main/java/com/example/SpringApi/Services'
base_tests_root = root / 'src/test/java/com/example/SpringApi/Services/Tests'

folders = ['Address','Login','UserLog','Todo']

allowed_annotations = {"Test","DisplayName","ExtendWith","Mock","Spy","InjectMocks","BeforeEach"}
mock_patterns = re.compile(r"@Mock|@Spy|@InjectMocks|\\bmock\\(|Mockito\\.mock")
inline_mock_pattern = re.compile(r"\\blenient\\(\\)\\.when\\(|\\bwhen\\(|\\bdoReturn\\(|\\bdoThrow\\(|\\bdoAnswer\\(|\\bdoNothing\\(\\)\\.when\\(|\\bdoCallRealMethod\\(")

# Parse ErrorMessages constants
err_path = root / 'src/main/java/com/example/SpringApi/ErrorMessages.java'
err_map = {}
if err_path.exists():
    err_text = err_path.read_text()
    class_stack = []
    const_re = re.compile(r"public static final String (\\w+) = \"(["'^'"\"]*)\";")
    for line in err_text.splitlines():
        class_match = re.match(r"\\s*public static class (\\w+)", line)
        if class_match:
            class_stack.append(class_match.group(1))
            continue
        if line.strip()=='}' and class_stack:
            class_stack.pop()
        m = const_re.search(line)
        if m:
            name, value = m.group(1), m.group(2)
            if class_stack:
                const_name = 'ErrorMessages.' + '.'.join(class_stack) + '.' + name
            else:
                const_name = 'ErrorMessages.' + name
            err_map.setdefault(value, []).append(const_name)


def suggest_constant(literal):
    if not literal:
        return None
    return err_map.get(literal, [None])[0]


def to_pascal(name:str)->str:
    return name[0].upper()+name[1:] if name else name


def parse_public_methods(service_path:Path):
    methods=[]
    if not service_path.exists():
        return methods
    lines=service_path.read_text().splitlines()
    class_name=service_path.stem
    method_re=re.compile(r""'^'"\\s*public\\s+(?:static\\s+)?(?"'!class|interface|enum)(['"\\w<>\\[\\],.?\\s]+?)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(")
    depth=0
    for i,line in enumerate(lines, start=1):
        depth_before=depth
        if depth_before==1:
            m=method_re.match(line)
            if m:
                name=m.group(2)
                if name"'!=class_name:
                    methods.append({'"'name':name,'line':i})
        depth += line.count('{') - line.count('}')
    return methods


def find_class_brace(lines):
    for i,line in enumerate(lines):
        if re.search(r"\\bclass\\b", line):
            if '{' in line:
                return i
            for j in range(i+1,len(lines)):
                if '{' in lines[j]:
                    return j
            break
    return None


def extract_method_body(lines, signature_line):
    i=signature_line-1
    brace_count=0
    started=False
    body=[]
    start_line=None
    for j in range(i, len(lines)):
        line=lines[j]
        if not started:
            if '{' in line:
                started=True
                brace_count += line.count('{')
                brace_count -= line.count('}')
                start_line=j+1
                body.append(line)
                if brace_count==0:
                    return body,start_line,j+1
            else:
                body.append(line)
        else:
            body.append(line)
            brace_count += line.count('{')
            brace_count -= line.count('}')
            if brace_count==0:
                return body,start_line,j+1
    return body,start_line or signature_line,len(lines)


def parse_assert_helpers(base_test_path:Path):
    helpers=set()
    if not base_test_path.exists():
        return helpers
    lines=base_test_path.read_text().splitlines()
    method_re=re.compile(r""'^'"\\s*(?:public|protected)\\s+\\w[\\w<>\\[\\]]*\\s+(assertThrows[A-Za-z0-9_]*)\\s*\\(")
    for line in lines:
        m=method_re.match(line)
        if m:
            helpers.add(m.group(1))
    return helpers


def has_doc_block(lines, test_line_index):
    j=test_line_index-1
    while j>=0 and lines[j].strip()=="":
        j-=1
    if j<0:
        return False
    if lines[j].strip()"'!="*/":
        return False
    k=j-1
    while k>=0:
        stripped=lines[k].strip()
        if stripped.startswith("/*"):
            return True
        if stripped.startswith("@Test") or re.match(r"'"''"'^'"\\s*(public|protected|private)?\\s*(static\\s+)?[\\w<>\\[\\]]+\\s+[A-Za-z0-9_]+\\s*\\(", lines[k]):
            return False
        k-=1
    return False


def suggest_rename(name, expected_prefix):
    failure_keywords=["Throw","Throws","Exception","Invalid","NotFound","Unauthorized","Forbidden","Fail","Failure","Error","Denied"]
    if '_' not in name:
        return f"{expected_prefix}_{name}_Success"
    if name.count('_')<2:
        outcome="Failure" if any(k.lower() in name.lower() for k in failure_keywords) else "Success"
        return f"{name}_{outcome}"
    if not name.startswith(expected_prefix+"_"):
        suffix=name[name.find('_')+1:]
        return f"{expected_prefix}_{suffix}"
    return name


def analyze_test_file(path:Path, base_assert_helpers:set, service_method_name:str):
    lines=path.read_text().splitlines()
    violations=[]
    meta={}
    # package
    package_line = next((i+1 for i,l in enumerate(lines) if l.strip().startswith('package ')), None)
    meta['package_line']=package_line
    meta['package'] = lines[package_line-1].strip().replace('package ','"''"').replace(';','"''"') if package_line else None

    # class line
    class_line=None
    class_decl=None
    for i,l in enumerate(lines, start=1):
        if re.search(r"\\bclass\\b", l):
            class_line=i
            class_decl=l.strip()
            break
    meta['class_line']=class_line
    meta['class_decl']=class_decl
    extends=None
    if class_decl and 'extends' in class_decl:
        extends=class_decl.split('extends',1)[1].strip().split()[0]
    meta['extends']=extends

    total_decl_lines=[(i+1,l) for i,l in enumerate(lines) if "Total Tests:" in l]
    meta['total_decl_lines']=total_decl_lines
    brace_idx=find_class_brace(lines)
    first_nonempty=None
    if brace_idx is not None:
        for j in range(brace_idx+1, len(lines)):
            if lines[j].strip()=="":
                continue
            first_nonempty=(j+1, lines[j])
            break
    decl_value=None
    if first_nonempty and re.match(r""'^'"\\s*//\\s+Total Tests:\\s+\\d+\\s*"'$", first_nonempty[1]):
        decl_value=int(re.findall(r"'"\\d+", first_nonempty[1])[0])
    meta['decl_value']=decl_value
    meta['first_nonempty']=first_nonempty

    test_lines=[i for i,l in enumerate(lines) if re.match(r""'^'"\\s*@Test\\b", l.strip())]
    meta['test_count']=len(test_lines)

    test_methods=[]
    for idx in test_lines:
        j=idx+1
        while j < len(lines):
            line=lines[j].strip()
            if line.startswith("@") or line=="":
                j+=1
                continue
            m=re.match(r""'^'"\\s*(public|protected|private)?\\s*(static\\s+)?[\\w<>\\[\\]]+\\s+([A-Za-z0-9_]+)\\s*\\(", lines[j])
            if m:
                test_methods.append({"name":m.group(3),"line":j+1,"test_line":idx+1})
            break
    meta['test_methods']=test_methods

    # Rule 2
    if decl_value is None:
        violations.append({"rule":2,"type":"missing_or_misplaced_total_tests","line":first_nonempty[0] if first_nonempty else None})
    else:
        if decl_value "'!= len(test_lines):
            violations.append({"rule":2,"type":"count_mismatch","declared":decl_value,"actual":len(test_lines),"line":first_nonempty[0] if first_nonempty else None})
        if len(total_decl_lines) > 1:
            violations.append({"rule":2,"type":"duplicate_declaration","lines":[ln for ln,_ in total_decl_lines]})

    # Rule 4
    disallowed=[]
    for i,l in enumerate(lines, start=1):
        ls=l.strip()
        if not ls.startswith('"'@'):
            continue
        if ls.startswith('//@'):
            continue
        anno=ls.split('(')[0].lstrip('@')
        if anno not in allowed_annotations:
            disallowed.append((i,anno))
    if disallowed:
        violations.append({"rule":4,"type":"disallowed_annotations","items":disallowed})

    # Rule 6
    mock_hits=[]
    for i,l in enumerate(lines, start=1):
        if mock_patterns.search(l):
            mock_hits.append((i,l.strip()))
    if mock_hits:
        violations.append({"rule":6,"type":"mock_in_test_file","items":mock_hits})

    # Rule 3
    controller_permission_tests=[tm for tm in test_methods if "_controller_permission_" in tm['name']]
    if not controller_permission_tests:
        violations.append({"rule":3,"type":"missing_controller_permission_test"})
    controller_issues=[]
    for tm in controller_permission_tests:
        body_lines, body_start, body_end = extract_method_body(lines, tm['line'])
        body_text="\\n".join(body_lines)
        if "Controller" not in body_text and "controller" not in body_text:
            controller_issues.append({"name":tm['name'],"line":tm['line'],"issue":"no_controller_invocation_detected"})
        if "HttpStatus" not in body_text and "statusCode" not in body_text:
            controller_issues.append({"name":tm['name'],"line":tm['line'],"issue":"no_http_status_assertion_detected"})
    if controller_issues:
        violations.append({"rule":3,"type":"invalid_controller_permission_tests","items":controller_issues})

    # Rule 5
    naming_violations=[]
    for tm in test_methods:
        name=tm['name']
        if service_method_name and not name.startswith(service_method_name+"_"):
            naming_violations.append({"name":name,"line":tm['line'],"issue":"does_not_start_with_method"})
        if name.count('_') < 2:
            naming_violations.append({"name":name,"line":tm['line'],"issue":"not_three_segments"})
    if naming_violations:
        violations.append({"rule":5,"type":"test_naming","items":naming_violations})

    # Rule 9
    doc_violations=[]
    for tm in test_methods:
        if not has_doc_block(lines, tm['test_line']-1):
            doc_violations.append({"name":tm['name'],"line":tm['test_line']})
    if doc_violations:
        violations.append({"rule":9,"type":"missing_doc_block","items":doc_violations})

    # Rule 12
    aaa_violations=[]
    for tm in test_methods:
        body_lines, body_start, body_end = extract_method_body(lines, tm['line'])
        has_arrange=False
        has_act=False
        has_assert=False
        for l in body_lines:
            s=l.strip()
            if not s.startswith('//'):
                continue
            if 'Arrange' in s:
                has_arrange=True
            if 'Act & Assert' in s:
                has_act=True
                has_assert=True
            else:
                if 'Act' in s:
                    has_act=True
                if 'Assert' in s:
                    has_assert=True
        if not (has_arrange and has_act and has_assert):
            aaa_violations.append({"name":tm['name'],"line":tm['line'],"missing":{
                'Arrange': not has_arrange,
                'Act': not has_act,
                'Assert': not has_assert
            }})
    if aaa_violations:
        violations.append({"rule":12,"type":"missing_aaa_comments","items":aaa_violations})

    # Rule 14 (inline mocks)
    inline_violations=[]
    for tm in test_methods:
        body_lines, body_start, body_end = extract_method_body(lines, tm['line'])
        for i,l in enumerate(body_lines, start=body_start):
            s=l.strip()
            if s.startswith('//') or s.startswith('*'):
                continue
            if inline_mock_pattern.search(l):
                inline_violations.append({"name":tm['name'],"line":i,"code":l.strip()})
    if inline_violations:
        violations.append({"rule":14,"type":"inline_mocks","items":inline_violations})

    # Rule 7 exception message
    exc_violations=[]
    for tm in test_methods:
        body_lines, body_start, body_end = extract_method_body(lines, tm['line'])
        body_text="\\n".join(body_lines)
        if 'assertThrows(' in body_text:
            uses_helper=False
            for helper in base_assert_helpers:
                if helper+'(' in body_text:
                    uses_helper=True
                    break
            if not uses_helper and 'getMessage()' not in body_text:
                for i,l in enumerate(body_lines, start=body_start):
                    if 'assertThrows(' in l:
                        exc_violations.append({"name":tm['name'],"line":i})
                        break
    if exc_violations:
        violations.append({"rule":7,"type":"missing_exception_message_assert","items":exc_violations})

    # Rule 8 hardcoded error strings
    err_const_violations=[]
    for i,l in enumerate(lines, start=1):
        if 'getMessage()' in l:
            if re.search(r'"["'^'"\"]+"', l):
                window_lines=lines[max(0,i-3):min(len(lines),i+2)]
                if any('ErrorMessages' in wl for wl in window_lines):
                    continue
                err_const_violations.append({"line":i,"code":l.strip()})
        if 'contains(' in l and 'getMessage' in l:
            if re.search(r'"["'^'"\"]+"', l):
                window_lines=lines[max(0,i-3):min(len(lines),i+2)]
                if any('ErrorMessages' in wl for wl in window_lines):
                    continue
                err_const_violations.append({"line":i,"code":l.strip()})
    if err_const_violations:
        violations.append({"rule":8,"type":"hardcoded_error_strings","items":err_const_violations})

    # Rule 10 sections
    section_lines=[]
    for i,l in enumerate(lines, start=1):
        if re.search(r"SUCCESS.*TESTS", l, re.IGNORECASE):
            section_lines.append((i,'SUCCESS'))
        elif re.search(r"(FAILURE|EXCEPTION).*TESTS", l, re.IGNORECASE):
            section_lines.append((i,'FAILURE'))
        elif re.search(r"(PERMISSION|AUTHORIZATION).*TESTS", l, re.IGNORECASE):
            section_lines.append((i,'PERMISSION'))
    required={'SUCCESS','FAILURE','PERMISSION'}
    present=set(s for _,s in section_lines)
    if required - present:
        violations.append({"rule":10,"type":"missing_sections","missing":sorted(list(required-present))})
    first_occ={}
    for line,sec in section_lines:
        if sec not in first_occ:
            first_occ[sec]=line
    if required.issubset(first_occ):
        order=[sec for sec,_ in sorted(first_occ.items(), key=lambda x:x[1])]
        if order "'!= ['"'SUCCESS','FAILURE','PERMISSION']:
            violations.append({"rule":10,"type":"section_order","order":order})
    if test_methods and section_lines and required.issubset(present):
        headers_sorted=sorted(section_lines, key=lambda x:x[0])
        for sec in ['SUCCESS','FAILURE','PERMISSION']:
            names=[]
            for tm in test_methods:
                header=None
                for line,sec_name in headers_sorted:
                    if line < tm['test_line']:
                        header=sec_name
                    else:
                        break
                if header==sec:
                    names.append((tm['name'], tm['line']))
            if len(names)>1:
                sorted_names=sorted([n for n,_ in names], key=lambda s:s.lower())
                current=[n for n,_ in names]
                if current"'!=sorted_names:
                    violations.append({"rule":10,"type":"not_alphabetical","section":sec,"names":names})

    # Rule 11 coverage by test names
    success_tests=[tm for tm in test_methods if re.search(r"_success", tm['"'name'], re.IGNORECASE)]
    failure_tests=[tm for tm in test_methods if re.search(r"_exception|_throws|_notFound|_invalid|_error|_unauthorized|_forbidden", tm['name'], re.IGNORECASE)]
    if test_methods and (not success_tests or not failure_tests):
        violations.append({"rule":11,"type":"coverage_by_name","missing_success": len(success_tests)==0, "missing_failure": len(failure_tests)==0})

    return meta, violations


def analyze_base_test(path:Path):
    violations=[]
    if not path.exists():
        return violations
    lines=path.read_text().splitlines()
    for i,line in enumerate(lines):
        if '@BeforeEach' in line:
            j=i+1
            while j < len(lines) and (lines[j].strip()=='"''"' or lines[j].strip().startswith('@')):
                j+=1
            if j>=len(lines):
                continue
            body, start, end = extract_method_body(lines, j+1)
            for idx,l in enumerate(body, start=start):
                s=l.strip()
                if s.startswith('//') or s.startswith('*'):
                    continue
                if inline_mock_pattern.search(l):
                    violations.append({'rule':14,'type':'inline_mock_in_before_each','line':idx,'code':l.strip()})
    method_re=re.compile(r""'^'"\\s*(public|protected|private)\\s+(static\\s+)?[\\w<>\\[\\]]+\\s+([A-Za-z0-9_]+)\\s*\\(")
    for i,line in enumerate(lines):
        m=method_re.match(line)
        if not m:
            continue
        name=m.group(3)
        if name==path.stem:
            continue
        body, start, end = extract_method_body(lines, i+1)
        if any(inline_mock_pattern.search(l) for l in body):
            if not name.startswith('stub'):
                violations.append({'rule':13,'type':'mocking_method_not_stub','line':i+1,'method':name})
    return violations

# Build analysis
reports={}
for folder in folders:
    service_path = service_dir / f"{folder}Service.java"
    test_folder = base_tests_root / folder
    base_test_path = test_folder / f"{folder}ServiceTestBase.java"

    methods = parse_public_methods(service_path)
    expected = {to_pascal(m['name'])+'Test.java':m for m in methods}

    test_files = []
    if test_folder.exists():
        test_files = [p for p in test_folder.glob('*Test.java') if not p.name.endswith('ServiceTestBase.java')]

    actual = {p.name:p for p in test_files}
    missing = [ {**m, 'expected_file': to_pascal(m['name'])+'Test.java'} for name,m in expected.items() if name not in actual ]
    extra = [ name for name in actual.keys() if name not in expected ]

    base_assert_helpers = parse_assert_helpers(base_test_path)
    file_reports={}
    for name,p in actual.items():
        method_prefix = p.stem
        if method_prefix.endswith('Test'):
            method_prefix = method_prefix[:-4]
        if method_prefix:
            method_prefix = method_prefix[0].lower()+method_prefix[1:]
        meta, violations = analyze_test_file(p, base_assert_helpers, method_prefix)
        file_reports[str(p)]={'meta':meta,'violations':violations}

    base_violations = analyze_base_test(base_test_path)

    reports[folder] = {
        'service': str(service_path),
        'methods': methods,
        'missing': missing,
        'extra': extra,
        'files': file_reports,
        'base_test': str(base_test_path),
        'base_violations': base_violations,
    }

# Report generation
severity_by_rule={1:'CRITICAL',2:'CRITICAL',3:'CRITICAL',4:'HIGH',5:'MEDIUM',6:'HIGH',7:'HIGH',8:'HIGH',9:'MEDIUM',10:'MEDIUM',11:'HIGH',12:'MEDIUM',13:'HIGH',14:'CRITICAL'}
rule_desc={1:'One Test File per Method',2:'Test Count Declaration',3:'Controller Permission Test',4:'Test Annotations',5:'Test Naming Convention',6:'Centralized Mocking',7:'Exception Assertions',8:'Error Constants',9:'Test Documentation',10:'Test Ordering',11:'Complete Coverage',12:'Arrange/Act/Assert',13:'Stub Naming',14:'No Inline Mocks'}

created=[]
removed=[]
for folder,data in reports.items():
    base_viol = data['base_violations']
    file_viols = [(f,info) for f,info in data['files'].items() if info['violations']]
    has_violations = bool(file_viols or data['missing'] or data['extra'] or base_viol)

    report_path = base_tests_root / f"{folder.upper()}_UNIT_TEST_VERIFICATION_REPORT.md"

    if not has_violations:
        if report_path.exists():
            report_path.unlink()
            removed.append(str(report_path))
        continue

    rule_counts=Counter()
    total_violations=0
    for f,info in file_viols:
        for v in info['violations']:
            rule_counts[v['rule']]+=1
            total_violations+=1
    if data['missing']:
        rule_counts[1]+=len(data['missing'])
        total_violations+=len(data['missing'])
    if data['extra']:
        rule_counts[1]+=len(data['extra'])
        total_violations+=len(data['extra'])
    for v in base_viol:
        rule_counts[v['rule']]+=1
        total_violations+=1

    methods_count=len(data['methods'])
    test_files_found=len(data['files'])

    status='❌ RED' if total_violations>0 else '✅ GREEN'

    out_lines=[]
    out_lines.append(f"# UNIT TEST VERIFICATION REPORT — {folder}\\n")
    out_lines.append(""'```")
    out_lines.append("╔════════════════════════════════════════════════════════════╗")
    out_lines.append("║           UNIT TEST VERIFICATION REPORT                    ║")
    out_lines.append("║                                                            ║")
    out_lines.append(f"║  Status: {status:<52}║")
    out_lines.append(f"║  Services Analyzed: 1{'"' ' * 38}║")
    out_lines.append(f"║  Public Methods Found: {methods_count:<35}║")
    out_lines.append(f"║  Test Files Expected: {methods_count:<35}║")
    out_lines.append(f"║  Test Files Found: {test_files_found:<38}║")
    out_lines.append(f"║  Total Violations: {total_violations:<38}║")
    out_lines.append("╚════════════════════════════════════════════════════════════╝")
    out_lines.append(""'```")

    out_lines.append("'"\\nVIOLATIONS BY RULE:")
    out_lines.append("\\n| Rule | Description | Count |")
    out_lines.append("| --- | --- | --- |")
    for rule in range(1,15):
        if rule in rule_counts:
            out_lines.append(f"| {rule} | {rule_desc[rule]} | {rule_counts[rule]} |")

    if data['missing'] or data['extra']:
        out_lines.append("\\n\\n**MISSING/EXTRA TEST FILES (RULE 1)**")
        if data['missing']:
            for m in data['missing']:
                expected_path = str((base_tests_root / folder / m['expected_file']))
                out_lines.append("\\n"+"═"*70)
                out_lines.append(f"MISSING FILE: {m['expected_file']}")
                out_lines.append("═"*70)
                out_lines.append(f"Service: "'`{data['"'service']}"'`")
                out_lines.append(f"Method: `{m['"'name']}"'` (line {m['"'line']})")
                out_lines.append(f"Expected Test Path: "'`{expected_path}`")
                out_lines.append("Required Minimum Tests:")
                out_lines.append(f"- {m['"'name']}_success")
                out_lines.append(f"- {m['name']}_controller_permission_forbidden")
                out_lines.append("- Add failure tests for each validation/exception path in the service method body.")
                out_lines.append("Required Stubs:")
                out_lines.append("- Add stub methods in the base test class for each repository/service interaction in the method.")
        if data['extra']:
            for e in data['extra']:
                out_lines.append(f"Extra test file with no matching public method: "'`{e}`. Either rename it to match a public method or remove it.")

    if base_viol:
        out_lines.append("'"\\n\\n**BASE TEST FILE ISSUES**")
        out_lines.append(f"Base Test: "'`{data['"'base_test']}"'`")
        for v in base_viol:
            if v['"'rule']==14:
                out_lines.append(f"- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line {v['line']}: "'`{v['"'code']}"'`. Move this into a `stub...` method.")
            elif v['"'rule']==13:
                out_lines.append(f"- Rule 13 [HIGH]: method "'`{v['"'method']}"'` at line {v['"'line']} performs mocking but does not start with "'`stub`. Rename to `stub{v['"'method'][0].upper()+v['method'][1:]}"'` and update callers.")

    out_lines.append("'"\\n\\n**FILE-BY-FILE BREAKDOWN**")
    for f,info in file_viols:
        meta=info['meta']
        declared=meta['decl_value']
        actual=meta['test_count']
        decl_lines=meta['total_decl_lines']
        decl_line_str=decl_lines[0][0] if decl_lines else 'N/A'
        prefix=Path(f).stem
        method_prefix = prefix[:-4] if prefix.endswith('Test') else prefix
        method_prefix = method_prefix[0].lower()+method_prefix[1:] if method_prefix else method_prefix
        loc=len(Path(f).read_text().splitlines())
        try:
            mtime=os.stat(f).st_mtime
            last_modified=datetime.fromtimestamp(mtime).strftime('%Y-%m-%d %H:%M:%S')
        except Exception:
            last_modified='N/A'

        out_lines.append("\\n"+"="*70)
        out_lines.append(f"FILE: "'`{f}`")
        out_lines.append("="*70)
        out_lines.append(f"Package: {meta['"'package']}")
        out_lines.append(f"Class: {meta['class_decl']}")
        out_lines.append(f"Extends: {meta['extends']}")
        out_lines.append(f"Lines of Code: {loc}")
        out_lines.append(f"Last Modified: {last_modified}")
        out_lines.append(f"Declared Test Count: {declared if declared is not None else 'MISSING/MISPLACED'} (first occurrence line {decl_line_str})")
        out_lines.append(f"Actual @Test Count: {actual}")
        out_lines.append("\\nVIOLATIONS FOUND:")
        v_index=1
        for v in info['violations']:
            rule=v['rule']
            sev=severity_by_rule.get(rule,'MEDIUM')
            out_lines.append(f"\\nVIOLATION {v_index}: Rule {rule} - {rule_desc[rule]}")
            out_lines.append(f"- Severity: {sev}")
            if rule==2:
                if v['type']=='missing_or_misplaced_total_tests':
                    out_lines.append(f"- Line: {decl_line_str}")
                    out_lines.append("- Problem: "'`// Total Tests: X` is missing or not the first line inside class body.")
                    out_lines.append(f"- Required: Insert `// Total Tests: {actual}` immediately after the class opening brace.")
                elif v['"'type']=='count_mismatch':
                    out_lines.append(f"- Line: {v['line']}")
                    out_lines.append(f"- Current: {v['declared']}")
                    out_lines.append(f"- Required: {v['actual']}")
                elif v['type']=='duplicate_declaration':
                    out_lines.append(f"- Lines: {', '.join(map(str,v['lines']))}")
                    out_lines.append("- Required: Keep only the first declaration at the class start; remove duplicates.")
            elif rule==3:
                if v['type']=='missing_controller_permission_test':
                    out_lines.append("- Problem: No controller permission test found.")
                    out_lines.append(f"- Required: Add "'`{method_prefix}_controller_permission_forbidden` or `{method_prefix}_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.")
                else:
                    for item in v.get('"'items',[]):
                        out_lines.append(f"- Line: {item['line']} ({item['name']})")
                        out_lines.append(f"- Problem: {item['issue'].replace('_',' ')}")
                        out_lines.append("- Required: Call controller method and assert HTTP status.")
            elif rule==4:
                for item in v['items']:
                    out_lines.append(f"- Line: {item[0]} has disallowed annotation @{item[1]}.")
                    out_lines.append("- Required: Remove or replace with allowed annotations only.")
            elif rule==5:
                for item in v['items']:
                    suggested=suggest_rename(item['name'], method_prefix)
                    out_lines.append(f"- Line: {item['line']} method "'`{item['"'name']}"'`")
                    out_lines.append(f"- Required rename: `{suggested}`")
            elif rule==6:
                for item in v['"'items']:
                    out_lines.append(f"- Line: {item[0]} has mock usage "'`{item[1]}`")
                    out_lines.append("- Required: Move mocks to base test file.")
            elif rule==7:
                for item in v['"'items']:
                    out_lines.append(f"- Line: {item['line']} in "'`{item['"'name']}"'`")
                    out_lines.append("- Required: Capture exception and assert exact message using ErrorMessages constant.")
            elif rule==8:
                for item in v['"'items']:
                    literal_match=re.search(r'"(["'^'"\"]+)"', item['code'])
                    literal=literal_match.group(1) if literal_match else None
                    suggestion=suggest_constant(literal)
                    out_lines.append(f"- Line: {item['line']} has hardcoded message: "'`{item['"'code']}"'`")
                    if suggestion:
                        out_lines.append(f"- Required: Replace with `{suggestion}`.")
                    else:
                        out_lines.append("- Required: Replace with an ErrorMessages constant (add one if missing).")
            elif rule==9:
                names=", ".join([f"{it['"'name']} (line {it['line']})" for it in v['items']])
                out_lines.append(f"- Missing documentation blocks for: {names}")
                out_lines.append("- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.")
            elif rule==10:
                if v['type']=='missing_sections':
                    out_lines.append(f"- Missing sections: {', '.join(v['missing'])}")
                    out_lines.append("- Required: Add Success, Failure, Permission section headers.")
                elif v['type']=='section_order':
                    out_lines.append(f"- Current order: {v['order']}")
                    out_lines.append("- Required: Success → Failure → Permission.")
                elif v['type']=='not_alphabetical':
                    current=[n for n,_ in v['names']]
                    sorted_names=sorted(current, key=lambda s:s.lower())
                    out_lines.append(f"- Section {v['section']} not alphabetical.")
                    out_lines.append("- Current order: " + ", ".join(current))
                    out_lines.append("- Required order: " + ", ".join(sorted_names))
            elif rule==11:
                out_lines.append("- Coverage by test names is incomplete.")
                if v.get('missing_success'):
                    out_lines.append("- Missing: at least one *_success test.")
                if v.get('missing_failure'):
                    out_lines.append("- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).")
            elif rule==12:
                for item in v['items']:
                    missing=[k for k,val in item['missing'].items() if val]
                    out_lines.append(f"- Line: {item['line']} in "'`{item['"'name']}"'` missing AAA comments: {'"', '.join(missing)}")
                    out_lines.append("- Required: Add "'`// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).")
            elif rule==14:
                for item in v['"'items']:
                    out_lines.append(f"- Line: {item['line']} inline mock in "'`{item['"'name']}"'`: `{item['"'code']}"'`")
                    out_lines.append("- Required: Move to base test stub method and call stub in test.")
            v_index+=1

        out_lines.append("'"\\nREQUIRED FIXES SUMMARY:")
        for v in info['violations']:
            out_lines.append(f"- Fix Rule {v['rule']} issues above.")

    out_lines.append("\\n\\n**IMPLEMENTATION PLAN (STEP-BY-STEP)**")
    step=1
    for f,info in file_viols:
        out_lines.append(f"{step}. Update "'`{f}` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.")
        step+=1
    if data['"'missing']:
        for m in data['missing']:
            expected_path = str((base_tests_root / folder / m['expected_file']))
            out_lines.append(f"{step}. Create "'`{expected_path}` for service method `{m['"'name']}"'` (line {m['"'line']} in "'`{data['"'service']}"'`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.")
            step+=1
    if data['"'extra']:
        for e in data['extra']:
            out_lines.append(f"{step}. Resolve extra test file "'`{e}` by renaming it to match a public method or removing it.")
            step+=1
    if base_viol:
        out_lines.append(f"{step}. Fix base test `{data['"'base_test']}"'` violations noted above.")
        step+=1

    out_lines.append("'"\\nVerification Commands (run after fixes):")
    for f,_ in file_viols[:6]:
        out_lines.append(f"- mvn -Dtest={Path(f).stem} test")

    report_path.write_text("\\n".join(out_lines))
    created.append(str(report_path))

print('UPDATED/CREATED:')
print('\\n'.join(created))
print('\\nDELETED:')
print('\\n'.join(removed))