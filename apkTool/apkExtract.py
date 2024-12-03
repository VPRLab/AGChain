#!/usr/bin/python
import re
from subprocess import Popen, PIPE
import sys
from androguard.core.bytecodes.apk import APK
import importlib

importlib.reload(sys)
def get_apk_info(file_path):
    """
    output:
    {
        'package_name': '',
        'version_number: '',
        'min_sdk': '',
        'target_sdk': '',
        'certificates': [
            {'issuer': '', 'subject': ''},
            {'issuer': '', 'subject': ''},
            ...
        ]
    }
    :param file_path:
    :return:
    """

    a = APK(file_path)

    # ATTENTION: THE PATH OF AAPT SHOULD BE CHECKED BEFORE DEPLOY
    p1 = Popen(['aapt', 'dump', 'badging', file_path], stdout=PIPE)
    (stdout, error) = p1.communicate()
    stdout = stdout.decode().strip().split('\n')

    if len(stdout) <= 2:
        stdout = ['', '', '']

    match_0 = re.compile("package: name='(\\S+)' versionCode='(\\d+)' versionName='(\\S+)'").match(stdout[0])
    match_1 = re.compile("sdkVersion:'(\\d+)'").match(stdout[1])
    match_2 = re.compile("targetSdkVersion:'(\\d+)'").match(stdout[2])

    if match_0 and match_1 and match_2:
        package_name = match_0.group(1)
        version_number = match_0.group(3)
        min_sdk = match_1.group(1)
        target_sdk = match_2.group(1)
    else:
        package_name = a.get_package()
        version_number = a.get_androidversion_name()
        min_sdk = a.get_min_sdk_version()
        target_sdk = a.get_target_sdk_version()

    if a.is_signed():
        try:
            certificates = [
                {'issuer': str(cert.issuer.human_friendly),
                 'subject': str(cert.subject.human_friendly),
                 'serial_number': str(cert.serial_number)}
                for cert in a.get_certificates()
            ]
        except UnicodeEncodeError as e:
            # some apk files' certificates' issuer and subject can not be decoded,
            # return empty list instead.
            certificates = []
    else:
        certificates = []

    apk_info_dict = {
        'package_name': package_name,
        'version_number': version_number,
        'min_sdk': min_sdk,
        'target_sdk': target_sdk,
        'certificates': certificates
    }

    return apk_info_dict


def test():
    """

    :return:
    
    filename_0 = "/Users/xiao/IdeaProjects/appchaincode/apkTool/com.qiyi.video_800110450.apk"
    filename_1 = "/Users/xiao/IdeaProjects/appchaincode/apkTool/223d9aa02d8732fb64169b83c8ab76df.apk"
    filename_2 = "/Users/xiao/IdeaProjects/appchaincode/apkTool/8548-2020-04-15033143-1586935903718.apk"
    filename_3 = "/Users/xiao/IdeaProjects/appchaincode/apkTool/com.iflytek.inp.apk"
    """
    
    filename_0 = "/Users/xiao/IdeaProjects/appchaincode/apkTool/com.qiyi.video_800110450.apk"
    filename_1 = "/Users/xiao/IdeaProjects/appchaincode/apkTool/223d9aa02d8732fb64169b83c8ab76df.apk"
    filename_2 = "/Users/xiao/IdeaProjects/appchaincode/apkTool/8548-2020-04-15033143-1586935903718.apk"
    filename_3 = "/Users/xiao/IdeaProjects/appchaincode/apkTool/com.iflytek.inp.apk"
    filename_4 = "/media/psf/Home/Dropbox/1_ApkChain/repackApps/09839A21278829220FB6144EBC06A47825086A345D33A65D9200AD9CB8BE61F9.apk"

    result = get_apk_info(filename_4)

    print(result)

    return None


if __name__ == '__main__':
    # test()
    a = []
    for i in range(1, len(sys.argv)):
        a.append((sys.argv[i]))
    path = a[0]
    print(get_apk_info(path))
