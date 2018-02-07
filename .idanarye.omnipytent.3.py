from omnipytent import *
from omnipytent.integration.plumbum import local
from omnipytent.execution import ShellCommandExecuter
from omnipytent.completers import file_completer


if FN.exists('g:ale_java_javac_classpath') and not VAR['g:ale_java_javac_classpath']:
    VAR['g:ale_java_javac_classpath'] = FN['javacomplete#server#GetClassPath']()


@ShellCommandExecuter
def ERUN(command):
    CMD.Erun.bang(command)


gradle = local['gradle']['-q']
run_py = local['./run.py']


def subproject_names():
    for path in local.path('.'):
        if (path / 'src').exists():
            yield path.basename


def gradle_tests(test):
    for subproject_name in subproject_names():
        if test.startswith('nisui.%s.' % subproject_name):
            cmd = local['gradle'][':%s:test' % subproject_name]
            break
    else:
        cmd = local['gradle']['test']
    cmd = cmd['--tests', test]
    return cmd


@task
def compile(ctx):
    gradle['build']['-x', 'test'] & ERUN


@task
def run(ctx):
    # cmd = run_py['experiment', 'info']
    # cmd = run_py['experiment', 'run']['transmittionRadius=10', 'markerRadius=20']
    # cmd = run_py['experiment', 'run']['faces=20']
    # cmd = run_py['dp', 'add']['transmittionRadius=10', 'markerRadius=20']
    # cmd = run_py['dp', 'add']['faces=5', 'num=10', '-n', 21]

    cmd & BANG


@task
def act(ctx):
    cmd = run_py['--format=csv']['dp', 'list'] | local['column']['-ts,']

    cmd & BANG


@task
def clean(ctx):
    gradle['clean'] & BANG


@task
def test(ctx):
    # gradle_tests() & BANG
    # gradle_tests('nisui.core.BasicExperimentRunningTest') & ERUN
    # gradle_tests('nisui.h2_store.BuildTablesTest') & ERUN
    # gradle_tests('nisui.h2_store.FillAndReadDataTest') & ERUN
    # gradle_tests('nisui.core.DynamicValuesTest') & ERUN
    # gradle_tests('nisui.java_runner.JavaValuesTest') & ERUN
    # gradle_tests('nisui.java_runner.JavaExperimentTest') & ERUN
    # gradle_tests('nisui.cli.ExperimentCommandsTests') & ERUN
    gradle_tests('nisui.cli.DataPointCommandsTests') & ERUN


@task
def test_all(ctx):
    local['gradle']['test'] & BANG


@task
def edit_runner_file(ctx):
    CMD.edit('/home/idanarye/links/study/Thesis/VocalTerritorySimulator/RunWithNisui.java')


def lombok_jar_file():
    candidates = (local.path(p) for p in local['find']['/home/idanarye/.gradle', '-name', 'lombok-*.jar']().splitlines())
    return max(candidates, key=lambda c: c.basename)


@task
def jar(ctx):
    local['gradle']['fatJar'] & BANG


_vts_args = [
    '/home/idanarye/links/study/Thesis/VocalTerritorySimulator/RunWithNisui.java',
    '/home/idanarye/links/study/Thesis/VocalTerritorySimulator/build/VocalTerritorySimulator.jar',
]
_vts_data_points = '/home/idanarye/links/study/Thesis/VocalTerritorySimulator/data-points.txt'

@task
def test_with_vts(ctx):
    cmd = gradle['run']['-Pargs=%s' % ' '.join(_vts_args)]
    cmd = cmd < _vts_data_points
    cmd & TERMINAL_PANEL
