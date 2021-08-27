import requests
import requests as req

MODS = ['lithium', 'hydrogen', 'fabric-api', 'fabric-language-kotlin', 'ledger']
MC_VERSIONS = ['1.17.1', '1.17.1-rc2']


def get_mod_info(_mod_name):
    response = req.get("https://api.modrinth.com/api/v1/mod/" + _mod_name)
    return response.json()


def get_latest_version_info(_mod_info):
    response = req.get("https://api.modrinth.com/api/v1/mod/" + _mod_info['id'] + "/version")
    versions = response.json()

    for version_info in versions:
        for compatible_version in MC_VERSIONS:
            if compatible_version in version_info['game_versions']:
                return version_info

    raise RuntimeError("Cannot find a suitable version")


def download_version(version_info, name):
    file_info = version_info['files'][0]
    url = file_info['url']

    file_data = requests.get(url)
    with open(name + ".jar", 'wb') as output_file:
        output_file.write(file_data.content)


for mod_name in MODS:
    mod = get_mod_info(mod_name)
    print("Checking versions for " + mod['title'])
    version = get_latest_version_info(mod)
    print("Latest version is " + version['version_number'])
    download_version(version, mod_name)
