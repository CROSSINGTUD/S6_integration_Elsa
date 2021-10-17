#include <fstream>
#include <sstream>
#include <iostream>
#include <filesystem>
#include "webview.h"
#include "cxxopts.hpp"
#include "json.hpp"

using namespace std;

#ifdef WIN32
int WINAPI WinMain(HINSTANCE hInt, HINSTANCE hPrevInst, LPSTR lpCmdLine,
	int nCmdShow) {
	int argc = __argc;
	char** argv = __argv;
#else
int main(int argc, char* argv[]) {
#endif
	string begin_template_marker{ "Marker.beginGeneratedCode();" };
	string end_template_marker{"\t\tMarker.endGeneratedCode();"};
	cxxopts::Options options("CongniCrypt.ComposableCrypto Host", "A tool to generate and maintain cryptographic security solutions which comprise multiple, possibly nested, cryptographic components.");
	options.add_options()
		("a,assumptions", "Path to a directory that contains assumption profiles.", cxxopts::value<string>())
		("c,components", "Path to a directory that contains component definitions", cxxopts::value<string>())
		("s,solution", "Name of the security solution to configure", cxxopts::value<string>())
		("t,target", "Path to the Java source file to put the generated code in", cxxopts::value<string>());
	cxxopts::ParseResult args{ options.parse(argc, argv) };
	
	vector<string> assumption_profiles{};
	if (args.count("assumptions")) {
		for (const filesystem::directory_entry& entry : filesystem::directory_iterator(args["assumptions"].as<string>())) {
			if (entry.is_regular_file() && entry.path().extension() == ".json") {
				ifstream ifs(entry.path());
				stringstream buffer;
				buffer << ifs.rdbuf();
				assumption_profiles.push_back(buffer.str());
			}
		}
	}
	vector<string> component_description_files{};
	if (args.count("components")) {
		for (const filesystem::directory_entry& entry : filesystem::directory_iterator(args["components"].as<string>())) {
			if (entry.is_regular_file() && entry.path().extension() == ".json") {
				ifstream ifs(entry.path());
				stringstream buffer;
				buffer << ifs.rdbuf();
				component_description_files.push_back(buffer.str());
			}
		}
	}
	string target_code_file_path;
	string source_file_contents;
	if (args.count("target")) {
		target_code_file_path = args["target"].as<string>();
		ifstream ifs(target_code_file_path);
		stringstream buffer;
		buffer << ifs.rdbuf();
		source_file_contents = buffer.str();
	}
	
	webview::webview w(true, nullptr);
	w.set_title("CongniCrypt.ComposableCrypto Host Window");
	w.set_size(800, 600, WEBVIEW_HINT_NONE);

	w.init("window.runningInNativeEnvironment = true;");
	if (args.count("solution")) {
		w.init("window.getInitialRootComponentName = () => '" + args["solution"].as<string>() + "';");
	}
	w.bind("ccNativeLoadAssumptions", [&w, &assumption_profiles](string seq, string req, void* arg) {
		regex r("\\$");
		string js{"Promise.resolve(null)"};
		for (const string& assumption_profile : assumption_profiles) {
			js += ".then(_ => window.importAssumptionProfile(`";
			js += regex_replace(assumption_profile, r, "\\$");
			js += "`))";
		}
		js += ";";
		w.eval(js);
		w.resolve(seq, 0, "{}");
	}, nullptr);
	w.bind("ccNativeLoadComponentDescriptions", [&w, &component_description_files](string seq, string req, void* arg) {
		regex r("\\$");
		string js{ "Promise.resolve(null)" };
		for (const string& component_description_file : component_description_files) {
			js += ".then(_ => window.importComponentDescriptions(`";
			js += regex_replace(component_description_file, r, "\\$");
			js += "`))";
		}
		js += ";";
		w.eval(js);
		w.resolve(seq, 0, "{}");
		}, nullptr);
	w.bind("ccNativeInsertGeneratedCodeIntoSourceFile", [&w, &source_file_contents, &begin_template_marker, &end_template_marker, &target_code_file_path](string seq, string req, void* arg) {
		if (!target_code_file_path.empty()) {
			nlohmann::json js{ nlohmann::json::parse(req) };
			string code{ js.at(0).get<string>() };
			size_t begin_pos{ source_file_contents.find(begin_template_marker) };
			size_t end_pos{ source_file_contents.find(end_template_marker) };
			if (begin_pos != string::npos && end_pos != string::npos) {
				begin_pos += begin_template_marker.length();
				source_file_contents.erase(begin_pos, end_pos - begin_pos);
				source_file_contents.insert(begin_pos, code);
				ofstream out(target_code_file_path);
				out << source_file_contents;
				out.close();
			}
			else {
				cout << "Beginning or end marker not found. " << begin_pos << "    " << end_pos << endl;
			}
		}
		w.resolve(seq, 0, "{}");
		w.terminate();
	}, nullptr);


	// w.navigate("https://www.julius-hardt.de/ComposableCrypto/");
	w.navigate("https://localhost:44328/");
	w.run();
	return 0;
}

#ifdef WIN32
int main() {
	return WinMain(GetModuleHandle(NULL), NULL, GetCommandLineA(), SW_SHOWNORMAL);
}
#endif
